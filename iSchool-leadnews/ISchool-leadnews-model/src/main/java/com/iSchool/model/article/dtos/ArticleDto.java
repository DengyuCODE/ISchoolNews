package com.iSchool.model.article.dtos;

import com.iSchool.model.article.pojos.ApArticle;
import com.iSchool.model.article.pojos.ApArticle;
import lombok.Data;

@Data
public class ArticleDto  extends ApArticle {
    /**
     * 文章内容
     */
    private String content;
}