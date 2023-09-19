package com.iSchool.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.dtos.WmNewsDto;
import com.iSchool.model.wemedia.dtos.WmNewsPageReqDto;
import com.iSchool.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {
    /**
     * 查询文章
     * @param dto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto dto);

    /**
     * 发布文章或者修改保存为草稿
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDto dto);

}
