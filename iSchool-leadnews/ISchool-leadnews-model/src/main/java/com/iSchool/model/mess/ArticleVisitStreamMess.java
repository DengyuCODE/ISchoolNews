package com.iSchool.model.mess;

import lombok.Data;

@Data
public class ArticleVisitStreamMess {
    /**
     * 文章id
     */
    private Long articleId;
    /**
     * 阅读量
     */
    private int view;
    /**
     * 收藏量
     */
    private int collect;
    /**
     * 评论量
     */
    private int comment;
    /**
     * 点赞量
     */
    private int like;
}