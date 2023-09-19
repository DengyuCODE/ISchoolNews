package com.iSchool.model.behavior.dtos;

import lombok.Data;

@Data
public class ReadBehaviorDto {
    /**
     * 文章id
     */
    Long articleId;

    /**
     * 阅读次数
     */
    Short count;
}
