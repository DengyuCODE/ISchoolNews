package com.iSchool.apis.article;

import com.iSchool.apis.article.fallback.IArticleClientFallback;
import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-article",fallback = IArticleClientFallback.class)
public interface IArticleClient {
    /**
     * 保存文章远程调用接口
     */
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto);
}
