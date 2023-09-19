package com.iSchool.article.feign;

import com.iSchool.apis.article.IArticleClient;
import com.iSchool.article.service.ApArticleService;
import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService apArticleService;
    /**
     * 保存文章(审核通过后通过接口调用)
     * @param dto
     * @return
     */
    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) {
        return apArticleService.saveArticle(dto);
    }


}
