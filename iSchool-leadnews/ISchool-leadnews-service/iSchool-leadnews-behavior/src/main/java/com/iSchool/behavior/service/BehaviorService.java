package com.iSchool.behavior.service;

import com.iSchool.model.behavior.dtos.LikesBehaviorDto;
import com.iSchool.model.common.dtos.ResponseResult;

public interface BehaviorService {

    /**
     * 点赞
     * @param dto
     * @return
     */
    public ResponseResult likes(LikesBehaviorDto dto);
}
