package com.iSchool.model.behavior.dtos;

import lombok.Data;

//点赞
@Data
public class LikesBehaviorDto {
    /**
     * 文章id
     */
    Long articleId;

    /**
     * 0 点赞  1 取消点赞
     */
    Short operation;

    /**
     * 0 文章  1 动态  2 评论
     */
    Short type;
}
