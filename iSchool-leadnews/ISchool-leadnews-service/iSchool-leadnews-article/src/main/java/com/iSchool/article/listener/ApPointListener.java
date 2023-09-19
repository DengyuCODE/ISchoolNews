package com.iSchool.article.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iSchool.apis.user.IUserClient;
import com.iSchool.article.mapper.ApPointMapper;
import com.iSchool.article.service.ApPointService;
import com.iSchool.common.constants.ArticleConstants;
import com.iSchool.common.constants.CommonConstans;
import com.iSchool.common.redis.CacheService;
import com.iSchool.model.article.pojos.ApPoint;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ApPointListener {

    @Autowired
    private ApPointService pointService;

    @Resource
    private ApPointMapper pointMapper;

    @Autowired
    private IUserClient userClient;

    @Autowired
    private CacheService cacheService;

    //接收死信队列的消息
    @RabbitListener(queues = ArticleConstants.DLE_TTL_QUEUE)
    public void onMessage(ApPoint apPoint){
        log.info("接收到积分消息....+{}", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        //根据apPoints去数据库中查找对应的数据
        Integer currUserId = ApThreadLocalUtil.getUser().getId();
        ApPoint selectPoint = pointMapper.selectOne(new LambdaQueryWrapper<ApPoint>()
                .eq(ApPoint::getUserId, currUserId)
                .eq(ApPoint::getArticleId, apPoint.getArticleId()));
        //判断当前数据现在的status（0 无效，1有效）
        Integer status = selectPoint.getStatus();
        //如果本次积分获取状态为无效，则不做任何操作
        if(status.equals(0)){
            return;
        }
        //如果是有效,更新用户积分
        ApUser currUser = userClient.queryUserInfo(currUserId);
        Long points = currUser.getPoints();
        points += selectPoint.getPoints();
        currUser.setPoints(points);
        userClient.updateUser(currUser);

        //将该条数据添加进缓存,设置缓存时间为当前时间
        //拼接key
        Long articleId = selectPoint.getArticleId();
        Integer userId = ApThreadLocalUtil.getUser().getId();
        String key = CommonConstans.Cache_PREFIX + ArticleConstants.POINT_SUFFIX + userId;
        saveInCache(key,articleId,selectPoint);

        //更新用户当天获取积分的次数
        String readTimeKey = ArticleConstants.READ_TIME + currUserId;
        String value = cacheService.get(readTimeKey);
        if(value == null){
            cacheService.set(key,"1");
            //该天第一次，设置过期时间为第二天凌晨
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            //第二天凌晨零点的时间
            LocalDateTime nextDayDateTime = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
            //计算时间间隔
            Duration duration = Duration.between(now,nextDayDateTime);
            long seconds = duration.getSeconds();
            cacheService.expire(readTimeKey,seconds,TimeUnit.SECONDS);
        }else{
            cacheService.incrBy(key,1);//自增
        }
    }

    //更新缓存
    public Boolean saveInCache(String key, Long articleId, ApPoint apPoint){
        Map<String,Object> map = new HashMap<>();
        map.put("points",apPoint);//将对象存进去
        map.put("expire",System.currentTimeMillis());
        String value = JSON.toJSONString(map);
        //设置缓存时间为1天
        cacheService.hPut(key,articleId.toString(),value);
        cacheService.expire(key,1, TimeUnit.DAYS);
        return true;
    }
}
