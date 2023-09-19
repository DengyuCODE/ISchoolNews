package com.iSchool.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.iSchool.apis.schedule.IScheduleClient;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.TaskTypeEnum;
import com.iSchool.model.wemedia.pojos.WmNews;
import com.iSchool.utils.common.ProtostuffUtil;
import com.iSchool.wemedia.service.WmNewsTaskService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.iSchool.model.schedule.dtos.Task;

import java.util.Date;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient scheduleClient;

    /**
     * 将提交的文章放入消费队列
     * @param newsId
     * @param publishTime
     */
    @Override
    @Async
    public void addNewsToTask(Integer newsId, Date publishTime) {
        log.info("添加任务到延迟服务中----begin");
        Task task=new Task();
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        task.setExecuteTime(publishTime.getTime());
        //加入队列
        WmNews wmNews=new WmNews();
        wmNews.setId(newsId);
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        scheduleClient.addTask(task);
        log.info("添加任务到延迟服务中----end");
    }

    @Autowired
    private WmNewsAutoScanServiceImpl wmNewsAutoScanService;

    /**
     * 消费文章
     */
    @Scheduled(fixedRate = 1000)
    @Override
    @SneakyThrows
    public void scanNewsByTask() {
        log.info("文章审核---消费任务执行---begin---");

        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if(responseResult.getCode().equals(200) && responseResult.getData() != null){
            String json_str = JSON.toJSONString(responseResult.getData());
            Task task = JSON.parseObject(json_str, Task.class);
            byte[] parameters = task.getParameters();
            WmNews wmNews = ProtostuffUtil.deserialize(parameters, WmNews.class);
            System.out.println(wmNews.getId()+"-----------");
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
        log.info("文章审核---消费任务执行---end---");
    }
}
