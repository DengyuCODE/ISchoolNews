package com.iSchool.behavior.service;

import com.iSchool.model.behavior.dtos.ReadBehaviorDto;
import com.iSchool.model.common.dtos.ResponseResult;

public interface ApReadBehaviorService {

    /**
     * 保存阅读行为
     * @param dto
     * @return
     */
    public ResponseResult readBehavior(ReadBehaviorDto dto);
}
