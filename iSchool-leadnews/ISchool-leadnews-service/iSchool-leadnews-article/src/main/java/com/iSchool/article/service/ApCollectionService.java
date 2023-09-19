package com.iSchool.article.service;

import com.iSchool.model.article.dtos.CollectionBehaviorDto;
import com.iSchool.model.common.dtos.ResponseResult;

public interface ApCollectionService {

    /**
     * 收藏
     * @param dto
     * @return
     */
    public ResponseResult collection(CollectionBehaviorDto dto);
}
