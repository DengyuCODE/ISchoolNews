package com.iSchool.article.service;

import com.iSchool.model.article.pojos.ApArticle;
import org.springframework.stereotype.Service;

public interface ArticleFreemarkerService {
    public void buildArticleToMinIO(ApArticle article ,String content);
}
