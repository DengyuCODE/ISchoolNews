package com.iSchool.wemedia.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.wemedia.dtos.WmNewsDto;
import com.iSchool.model.wemedia.dtos.WmNewsPageReqDto;
import com.iSchool.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;

    /**
     * 查询文章
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto){
        return  wmNewsService.findAll(dto);
    }

    /**
     * 发布文章或者修改保存为草稿
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        return wmNewsService.submitNews(dto);
    }

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto){
        ResponseResult responseResult = wmNewsService.downOrUp(dto);
        return responseResult;
    }
}