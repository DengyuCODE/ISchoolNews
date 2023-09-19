package com.iSchool.model.behavior.dtos;

import lombok.Data;

import java.util.Date;

//文章收藏
@Data
public class CollectionBehaviorDto {
    /**
     * 文章id
     */
    Long entryId;

    /**
     * 0 收藏  1取消收藏
     */
    Short operation;

    /**
     * 发布时间
     */
    Date publishTime;

    /**
     * 0 文章  1 动态
     */
    Short type;
}
