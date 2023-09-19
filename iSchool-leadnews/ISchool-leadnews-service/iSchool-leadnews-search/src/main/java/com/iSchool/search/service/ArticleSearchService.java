package com.iSchool.search.service;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.search.dtos.UserSearchDto;

import java.io.IOException;

public interface ArticleSearchService {

    /**
     * es文章分页检索
     * @param userSearchDto
     * @return
     */
    public ResponseResult search(UserSearchDto userSearchDto) throws IOException;
}
