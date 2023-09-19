package com.iSchool.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.article.dtos.ArticleHomeDto;
import com.iSchool.model.article.dtos.ArticleInfoDto;
import com.iSchool.model.article.pojos.ApArticle;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.mess.ArticleVisitStreamMess;
import com.iSchool.model.article.pojos.ApPoint;

public interface ApArticleService extends IService<ApArticle> {
    /**
     * 加载文章列表
     * @param dto
     * @param type 1 加载更多 2 加载最新
     * @return
     */
    public ResponseResult load(ArticleHomeDto dto, Short type);

    /**
     * 更新定时热点数据后更新加载首页的操作，加载首页在redis中去加载数据
     * @param dto
     * @param type
     * @param firstPage
     * @return
     */
    public ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage);

    /**
     * 审核通过后保存文章
     * @param dto
     * @return
     */
    public ResponseResult saveArticle(ArticleDto dto);

    /**
     * 加载文章详情 数据回显
     * @param dto
     * @return
     */
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto);

    /**
     * 更新文章的分值  同时更新缓存中的热点文章数据
     * @param mess
     */
    public void updateScore(ArticleVisitStreamMess mess);
}
