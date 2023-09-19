package com.iSchool.model.user.dtos;

import com.iSchool.model.common.annotation.IdEncrypt;
import lombok.Data;

/**
 * 文章关注与取消
 */
@Data
public class UserRelationDto {

    // 文章作者ID
    @IdEncrypt
    Integer authorId;

    // 文章id
    @IdEncrypt
    Long articleId;
    /**
     * 操作方式
     * 0  关注
     * 1  取消
     */
    Short operation;
}