package com.iSchool.wemedia.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.wemedia.dtos.WmNewsDto;
import com.iSchool.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {
    @Autowired
    private WmChannelService wmChannelService;

    /**
     * 查询所有频道
     * @return
     */
    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    /**
     * 查看文章详情
     * @param id
     * @return
     */
    @GetMapping("/one/{id}")
    public ResponseResult viewOne(@PathVariable Integer id){
        return wmChannelService.viewOne(id);
    }

    /**
     * 删除文章
     * @param id
     * @return
     */
    @GetMapping("/del_news/{id}")
    public ResponseResult del_news(@PathVariable Integer id){
        return wmChannelService.del_news(id);
    }

    /**
     * 文章上下架 0 下架 1 上架
     * @param dto
     * @return
     */
   /* @PostMapping("/down_or_up")
    public ResponseResult down_or_up(WmNewsDto dto){
        return wmChannelService.down_or_up(dto);
    }*/
}