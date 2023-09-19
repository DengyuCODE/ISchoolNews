package com.iSchool.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.search.dtos.UserSearchDto;
import com.iSchool.model.search.pojos.ApUserSearch;
import com.iSchool.search.pojos.HistorySearchDto;

public interface ApUserSearchService {
    /**
     * 新增搜索记录
     * @param keyword
     * @param userId
     */
    public void insert(String keyword,Integer userId);

    /**
     * 获取搜索记录列表
     */
    public ResponseResult findUserSearch();

    /**
     * 删除搜索记录
     * @param dto
     */
    public ResponseResult deleteUserSearch(HistorySearchDto dto);
}