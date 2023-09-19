package com.iSchool.article.controller.v1;

import com.iSchool.article.service.ApArticleService;
import com.iSchool.model.article.dtos.ArticleInfoDto;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.article.pojos.ApPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleInfoController {

    @Autowired
    private ApArticleService apArticleService;

    /**
     * 加载文章行为-数据回显ApCollectionService
     * @param dto
     * @return
     */
    @PostMapping("/load_article_behavior")
    public ResponseResult loadArticleBehavior(@RequestBody ArticleInfoDto dto){
        return apArticleService.loadArticleBehavior(dto);
    }
}