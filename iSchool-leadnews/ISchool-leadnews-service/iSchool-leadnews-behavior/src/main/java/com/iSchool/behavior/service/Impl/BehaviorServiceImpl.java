package com.iSchool.behavior.service.Impl;

import com.iSchool.behavior.service.BehaviorService;
import com.iSchool.common.constants.BehaviorConstants;
import com.iSchool.common.redis.CacheService;
import com.iSchool.model.behavior.dtos.LikesBehaviorDto;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class BehaviorServiceImpl implements BehaviorService {

    @Autowired
    private CacheService cacheService; // 注:需要在nacos中配置redis的相关配置

    /**
     * 点赞
     * 设计思路：使用数据结构哈希表，然后field(字段)为用户id+文章id+type+点赞前缀,value为点赞状态。hash可以将该用户的所有的点赞记录都记录在一个hash表中，
     * 在指定时间(1小时或者2小时)将数据集体写入mysql表中保存,获取点赞次数就用scan模糊查询的方式(*章id+type+点赞前缀)去查询得到总数
     * @param dto
     * @return
     */
    @Override
    public ResponseResult likes(LikesBehaviorDto dto) {
        //判断参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断用户登录情况
        ApUser apUser = ApThreadLocalUtil.getUser();
        Integer userId = apUser.getId();
        if (apUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);//需要登录后操作
        }
        //拼接key
        String key=
                BehaviorConstants.LIKES_BEHAVIOR
                        +dto.getArticleId().toString()+"_" +userId.toString()+"_"+dto.getType().toString();

        //判断是点赞还是取消点赞
        Short operation = dto.getOperation();
        if(operation.equals(BehaviorConstants.LIKE)){
            //表示点赞，将数据存入redis

        }else if(operation.equals(BehaviorConstants.DIS_LIKE)){
            //表示取消点赞，直接删除redis中的相关key
        }
        //如果查询结果为null则初始化存入redis中

        //如果存在，判断点赞数据是否和dto中的一致
        //一致(便是打开文章时查询)
        //不一致(用户进行了点赞或者取消点赞操作)
        return null;
    }
}
