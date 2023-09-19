package com.iSchool.wemedia.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.common.constants.WemediaConstants;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.wemedia.dtos.WmNewsDto;
import com.iSchool.model.wemedia.pojos.WmChannel;
import com.iSchool.model.wemedia.pojos.WmNews;
import com.iSchool.wemedia.mapper.WmChannelMapper;
import com.iSchool.wemedia.service.WmChannelService;
import com.iSchool.wemedia.service.WmNewsService;
import com.iSchool.wemedia.mapper.WmChannelMapper;
import com.iSchool.wemedia.service.WmChannelService;
import com.iSchool.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    /**
     * 查询所有频道
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(this.list());
    }

    /**
     * 查看文章率详情
     * @param id
     * @return
     */
    @Autowired
    private WmNewsService wmNewsService;

    @Override
    public ResponseResult viewOne(Integer id) {
        //检查参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //根据id查询
        WmNews wmNew = wmNewsService.getOne(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId, id));
        //判断查询结果
        if(wmNew==null){
            return  ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(),"文章不存在");
        }
        return ResponseResult.okResult(wmNew);
    }

    /**
     * 删除文章
     * @param id
     * @return
     */
    @Override
    public ResponseResult del_news(Integer id) {
        //检查参数
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID.getCode(),"文章id不可缺少");
        }
        //根据id查找文章
        WmNews wmNew = wmNewsService.getOne(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId, id));
        //判断文章是否存在
        if(wmNew==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(),"文章不存在");
        }
        //判断文章是否发布
        if(wmNew.getStatus().equals((short)9)){
            //9表示文章已发布
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID.getCode(),"文章已发布，不能删除");
        }
        //删除
        wmNewsService.remove(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId,wmNew.getId()));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),"删除成功");
    }

    @Override
    public ResponseResult down_or_up(WmNewsDto dto) {
        //校验参数
        if(dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID.getCode(),"文章id不可缺少");
        }
        //获取其中文章id以及enable(上下架)
        //根据id查找文章
        Integer id = dto.getId();
        Short enable = dto.getEnable();
        WmNews wmNew = wmNewsService.getOne(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId, id));
        //判断获取文章情况
        if(wmNew == null){
            //文章不存在
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(),"文章不存在");
        }
        //判断当前文章是否发布，未发布不能上下架
        if(!wmNew.getStatus().equals((short)9)){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID.getCode(),"文章未发布，不能上下架");
        }
        //判断需要文章上架还是下架
        //文章下架操作
        if(enable.equals(WemediaConstants.WM_UN_ENABLE)){
            //判断文章实际上架情况
            if(wmNew.getEnable().equals(WemediaConstants.WM_UN_ENABLE)){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST.getCode(),"文章已经下架");
            }
            //更改文章上下架状态
            wmNew.setEnable(WemediaConstants.WM_ENABLE);
            wmNewsService.updateById(wmNew);
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }else {
            //判断文章实际上架情况
            if(wmNew.getEnable().equals(WemediaConstants.WM_ENABLE)){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST.getCode(),"文章已经上架");
            }
            //更改文章上下架状态
            wmNew.setEnable(WemediaConstants.WM_UN_ENABLE);
            wmNewsService.updateById(wmNew);
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
    }
}