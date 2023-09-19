package com.iSchool.behavior.service;

import com.iSchool.model.behavior.dtos.LikesBehaviorDto;
import com.iSchool.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorService {

    /**
     * 存储喜欢数据
     * @param dto
     * @return
     */
    public ResponseResult like(LikesBehaviorDto dto);
}
