package com.iSchool.search.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.search.dtos.UserSearchDto;
import com.iSchool.model.search.pojos.ApUserSearch;
import com.iSchool.search.service.ApUserSearchService;
import com.iSchool.search.service.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/article/search")
public class ArticleSearchController {

    @Autowired
    private ApUserSearchService apUserSearchService;

    @Autowired
    private ArticleSearchService articleSearchService;

    @PostMapping("search")
    public ResponseResult search(@RequestBody UserSearchDto userSearchDto) throws IOException {
        return articleSearchService.search(userSearchDto);
    }
}
