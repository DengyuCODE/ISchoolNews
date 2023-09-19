package com.iSchool.article.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.article.mapper.ApPointMapper;
import com.iSchool.article.service.ApPointService;
import com.iSchool.common.constants.ArticleConstants;
import com.iSchool.common.constants.CommonConstans;
import com.iSchool.common.exception.CustomException;
import com.iSchool.common.exception.ExceptionCatch;
import com.iSchool.common.redis.CacheService;
import com.iSchool.model.article.pojos.ApPoint;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ApPointServiceImpl extends ServiceImpl<ApPointMapper, ApPoint> implements ApPointService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ApPointMapper pointMapper;

    /**
     * 进入文章，开始计时
     * @param apPoints 包含用户id ,文章id , 积分数量 , 积分状态:默认有效(1)
     * @return
     */
    @Override
    public ResponseResult keepTime(ApPoint apPoints) {
        CustomException customException;
        if(apPoints == null){
            customException = new CustomException(AppHttpCodeEnum.PARAM_REQUIRE);
            ExceptionCatch exceptionCatch = new ExceptionCatch();
            exceptionCatch.exception(customException);
        }

        //返回给前端表示是否需要记录
        Map<String, Integer> param = new HashMap<>();
        param.put("flag",0);

        //判断缓存中是否存在该对象的缓存(缓存是否过期)
        //拼接key
        Long articleId = apPoints.getArticleId();
        Integer userId = ApThreadLocalUtil.getUser().getId();
        String key = CommonConstans.Cache_PREFIX + ArticleConstants.POINT_SUFFIX + userId;

        //判断当前key是否存在
        Boolean isExists = cacheService.exists(key);
        if(!isExists){
            //TODO:判断当前是否达到获取次数上限
            int times = queryReadTimes();
            //到达上限次数，返回flag = 0(如果未达到上限，后序成功获取到积分之后再次数加一)
            if(times >= ArticleConstants.LIMIT_TIMES){
                param.put("flag",0);
                return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
            }

            //key不存在，更新数据库
            if(saveInDatabase(apPoints)){
                //不更新到缓存，从交换机出来后再考虑更新到缓存
                //将数据发送到ttl交换机
                rabbitTemplate.convertAndSend(ArticleConstants.TTL_EXCHANGE,"tll.point",apPoints);
                log.info("积分消息开始发送...{}", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                param.put("flag",1);
                return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
            }
        }

        //key存在，判断hash中的当前文章的缓存时间是否超过了一天
        String value = (String)cacheService.hGet(key, articleId.toString());
        //hash表中存在当前articleId的字段
        if(value != null){
            Map<String,Object> map = JSON.parseObject(value, Map.class);
            //判断过期时间
            Long expire = (Long)map.get("expire");
            //获取当前时间
            long currentTime = System.currentTimeMillis();
            int time = (int)((currentTime - expire) / 3600000); //换算成时
            //如果缓存没有过期,返回前端一个参数表示该次阅读不计积分(flag = 0不计 1计)(一天内同一篇文章无法重复获取积分)
            if(time <= 24){
                param.put("flag",0);
                return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
            }
            //TODO:缓存过期,判断是否达到获取积分次数上限
            int times = queryReadTimes();
            //到达上限次数，返回flag = 0(如果未达到上限，后序成功获取到积分之后再次数加一)
            if(times >= ArticleConstants.LIMIT_TIMES){
                param.put("flag",0);
                return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
            }
            //将数据存入数据库中(保证每次阅读开始的时候获取积分的状态都为true)
            if(saveInDatabase(apPoints)){
                //不更新到缓存，从交换机出来后再考虑更新到缓存
                //将数据发送到ttl交换机
                rabbitTemplate.convertAndSend(ArticleConstants.TTL_EXCHANGE,"tll.point",apPoints);
                log.info("积分消息开始发送...{}",LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                param.put("flag",1);
                return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
            }
        }
        //不存在当前articleId的字段,直接存入数据库
        if(saveInDatabase(apPoints)){
            //不更新到缓存，从交换机出来后再考虑更新到缓存
            //将数据发送到ttl交换机
            rabbitTemplate.convertAndSend(ArticleConstants.TTL_EXCHANGE,"tll.point",apPoints);
            log.info("积分消息开始发送...{}",LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            param.put("flag",1);
            return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
        }

        return new ResponseResult(AppHttpCodeEnum.SUCCESS.getCode(),param);
    }

    //将本篇文章获取积分信息更新到积分表
    public Boolean saveInDatabase(ApPoint apPoints){
        try{
            //在表中查找userId和articleId与之匹配的数据
            ApPoint selectPoint = pointMapper.selectOne(new LambdaQueryWrapper<ApPoint>()
                    .eq(ApPoint::getUserId, ApThreadLocalUtil.getUser().getId())
                    .eq(ApPoint::getArticleId, apPoints.getArticleId()));
            //判断是否存在
            if(selectPoint == null){
                //不存在，新增
                apPoints.setUpdatedTime(new Date());//添加修改时间
                this.save(apPoints);
            }else{
                //存在，修改
                selectPoint.setStatus(1);//不管状态如何，都修改为(有效)
                selectPoint.setUpdatedTime(new Date());
                selectPoint.setPoints(apPoints.getPoints());
                this.updateById(selectPoint);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    //更新缓存
    public Boolean saveInCache(String key, Long articleId, ApPoint apPoints){
        Map<String,Object> map = new HashMap<>();
        map.put("points",apPoints);//将对象存进去
        map.put("expire",System.currentTimeMillis());
        String value = JSON.toJSONString(map);
        //设置缓存时间为1天
        cacheService.hPut(key,articleId.toString(),value);
        cacheService.expire(key,1, TimeUnit.DAYS);
        return true;
    }

    //判断是否达到获取上限
    int queryReadTimes(){
        int times = 0;//初始化次数
        int limitTimes = ArticleConstants.LIMIT_TIMES;//上限次数
        Integer currUserId = ApThreadLocalUtil.getUser().getId();
        String readTimeKey = ArticleConstants.READ_TIME + currUserId;
        String value = cacheService.get(readTimeKey);
        if(value != null){
            //存在key,获取当前次数
            times = Integer.parseInt(value);
        }
        return times;
    }


    /**
     * 关闭文章,获取阅读时长
     * @param articleId
     * @param readTime
     * @param flag
     * @return
     */
    @Override
    public ResponseResult readTime(Long articleId, Long readTime,Integer flag) {
        //判断flag == 0？ 如果等于零则直接返回(该篇文章不计分)
        if(flag == 0){
            return new ResponseResult();
        }
        //判断readTime是否大于指定时间
        if(readTime / 1000 < ArticleConstants.READING_TIME){
            //未达到指定时长，将积分表中的状态改为0(无效)
            ApPoint apPoint = pointMapper.selectOne(new LambdaQueryWrapper<ApPoint>()
                    .eq(ApPoint::getUserId, ApThreadLocalUtil.getUser().getId())
                    .eq(ApPoint::getArticleId, articleId));
            apPoint.setStatus(0);//状态改为(无效)
            apPoint.setUpdatedTime(new Date());
            this.updateById(apPoint);
            return new ResponseResult();
        }
        //大于指定时长，不做修改
        return new ResponseResult();
    }
}
