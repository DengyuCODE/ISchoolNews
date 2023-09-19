package com.iSchool.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.article.mapper.ApPointMapper;
import com.iSchool.model.article.pojos.ApPoint;
import com.iSchool.model.common.dtos.ResponseResult;

public interface ApPointService extends IService<ApPoint> {
    /**
     * 加载文章时计时
     * @param apPoints
     * @return
     */
    public ResponseResult keepTime(ApPoint apPoints);

    /**
     * 返回阅读时长
     * @param articleId
     * @param readTime
     * @param flag
     * @return
     */
    public ResponseResult readTime(Long articleId, Long readTime,Integer flag);
}
