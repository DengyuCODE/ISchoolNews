package com.iSchool.schedule.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iSchool.common.constants.ScheduleConstants;
import com.iSchool.common.redis.CacheService;
import com.iSchool.model.schedule.dtos.Task;
import com.iSchool.model.schedule.pojos.Taskinfo;
import com.iSchool.model.schedule.pojos.TaskinfoLogs;
import com.iSchool.schedule.mapper.TaskinfoLogsMapper;
import com.iSchool.schedule.mapper.TaskinfoMapper;
import com.iSchool.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Autowired
    private CacheService cacheService;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    /**
     * 添加任务
     * @param task 任务对象
     * @return 任务id
     */
    @Transactional
    @Override
    public void addTask(Task task) {
        log.info("有新的任务到来");
        //添加任务到数据库中
        boolean success = addTaskToDb(task);

        log.info("数据成功保存到数据库的情况:{}",success);
        log.info("任务id为:{}",task.getTaskId());
        //如果成功添加任务到数据库才添加任务到redis中
        if(success == true){
            addTaskToCache(task);
        }
        //return task.getTaskId();
    }

    /**
     * 添加任务到redis中
     * @param task
     */
    private void addTaskToCache(Task task) {
        //根据任务类型和任务优先级拼接一个key
        String key=task.getTaskType()+"_"+task.getPriority();

        //获取五分钟之后的时间 毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long nextScheduleTime = calendar.getTimeInMillis();

        //如果任务执行时间小于等于当前时间，放入list
        if(task.getExecuteTime()<= System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        }
        //如果任务执行时间大于当前时间但是小于当前时间加五分钟，放入zSet中
        else if(task.getExecuteTime()<=nextScheduleTime){
            //key,value,分数
            cacheService.zAdd(ScheduleConstants.FUTURE+key,JSON.toJSONString(task),task.getExecuteTime());
        }
        //如果大于当前时间加五分钟则不放入redis中
    }

    /**
     * 添加任务到数据库中
     * @param task
     */
    private boolean addTaskToDb(Task task) {
        boolean flag = false;

        try{
            //保存到任务表
            Taskinfo taskinfo =new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            //设置执行时间
            //task中执行时间是Long类型,taskinfo中是Date类型
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            //保存到任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            //设置版本(乐观锁)
            taskinfoLogs.setVersion(1);
            //设置状态(初始化,已经执行,已取消)0=SCHEDULED 1=EXECUTED 2=CANCELLED
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);

            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }


    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Transactional
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        //删除任务,更新日志
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);

        //删除redis中的任务
        if(task != null){
            removeTaskFromCache(task);
            flag= true;
        }
        return flag;
    }

    private void removeTaskFromCache(Task task) {
        //获得key
        String key =task.getTaskType()+"_"+task.getPriority();
        //判断执行时间
        //执行时间小于等于当前时间在list中删除
        if(task.getExecuteTime() <= System.currentTimeMillis()){
            //index> 0：删除等于从左到右移动的值的第一个元素；index< 0：删除等于从右到左移动的值的第一个元素；
            //index = 0：删除等于value的所有元素。
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else{
            //执行时间大于当前时间,在zSet中删除
            cacheService.zRemove(ScheduleConstants.FUTURE+key,JSON.toJSONString(task));
        }

    }

    private Task updateDb(long taskId,int status) {
        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            //修改日志表中的信息，将任务状态设置为取消
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            //获取task用于返回
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        }catch (Exception e){
            log.error("task cancel exception taskid={}",taskId);
        }

        return task;
    }

    /**
     * 按照type和优先级消费任务
     * @param type
     * @param priority
     * @return
     */
    @Transactional
    @Override
    public Task poll(int type, int priority) {
        //获取task
        Task task = null;
        String key = type+"_"+priority;
        String  task_json= cacheService.lRightPop(ScheduleConstants.TOPIC + key);
        task = JSON.parseObject(task_json,Task.class);
        //更新数据库
        //删除任务表中的数据
        //修改任务日志表对应数据的状态
        updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
        return task;
    }

    /**
     * 未来数据定时刷新
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){

        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            log.info("未来数据定时刷新---定时任务");

            //获取所有未来数据的集合key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {//future_100_50

                //获取当前数据的key  topic
                String topicKey = ScheduleConstants.TOPIC+futureKey.split(ScheduleConstants.FUTURE)[1];

                //按照key和分值查询符合条件的数据(大于0且小于当前时间)
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                //同步数据
                if(!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                    log.info("成功的将"+futureKey+"刷新到了"+topicKey);
                }
            }
        }
    }

    /**
     * //每五分钟将数据库中的任务刷新到缓存中
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    @PostConstruct
    public void reloadData() {
        //先将缓存中的数据清空避免重复加载
        clearCache();
        log.info("数据库数据同步到缓存");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        log.info("当前时间的五分钟后为:{}",calendar.getTime());

        //测试获取数据库中的数据
//        Taskinfo taskinfo1 = taskinfoMapper.selectOne(Wrappers.<Taskinfo>lambdaQuery().eq(Taskinfo::getTaskId, 1650033056569946114L));
//        log.info("数据库中取出的时间为:{}",taskinfo1.getExecuteTime());

        //查看小于未来5分钟的所有任务
        List<Taskinfo> allTasks =
                taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime,calendar.getTime()));
        if(allTasks != null && allTasks.size() > 0){
            for (Taskinfo taskinfo : allTasks) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }else {
            log.info("没有获取到数据库中的数据");
        }
    }
    private void clearCache(){
        // 删除缓存中未来数据集合和当前消费者队列的所有key
        Set<String> futurekeys = cacheService.scan(ScheduleConstants.FUTURE + "*");// future_
        Set<String> topickeys = cacheService.scan(ScheduleConstants.TOPIC + "*");// topic_
        cacheService.delete(futurekeys);
        cacheService.delete(topickeys);
        log.info("缓存中的数据已清空");
    }
}
