package com.iSchool.search.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.search.pojos.HistorySearchDto;
import com.iSchool.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
public class ApUserSearchController {

    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * 加载历史搜索列表
     * @return
     */
    @PostMapping("/load")
    public ResponseResult findUserSearch(){
        return apUserSearchService.findUserSearch();
    }

    /**
     * 删除指定历史记录
     * @param dto
     * @return
     */
    @PostMapping("/del")
    public ResponseResult deleteUserSearch(@RequestBody HistorySearchDto dto){
        return apUserSearchService.deleteUserSearch(dto);
    }
}
