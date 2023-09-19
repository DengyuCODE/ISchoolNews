package com.iSchool.user.service;


import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.user.dtos.UserRelationDto;


public interface ApUserRelationService {
    /**
     * 用户关注/取消关注
     * @param dto
     * @return
     */
    public ResponseResult follow(UserRelationDto dto);
}