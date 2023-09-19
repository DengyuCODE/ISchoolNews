package com.iSchool.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.dtos.WmNewsDto;
import com.iSchool.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {
    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();

    /**
     * 查看文章详情
     * @param id
     * @return
     */
    public ResponseResult viewOne(Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    public ResponseResult del_news(Integer id);

    /**
     * 文章上下架 0下架 1上架
     * @param dto
     * @return
     */
    public ResponseResult down_or_up(WmNewsDto dto);
}