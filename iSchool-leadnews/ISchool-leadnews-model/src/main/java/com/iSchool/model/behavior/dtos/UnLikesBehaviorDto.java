package com.iSchool.model.behavior.dtos;

import lombok.Data;

//不喜欢
@Data
public class UnLikesBehaviorDto {
    /**
     * 文章id
     */
    Long articleId;

    /**
     * 0 不喜欢  1 取消不喜欢
     */
    Short type;
}
