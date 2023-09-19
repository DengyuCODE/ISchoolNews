package com.iSchool.wemedia.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.dtos.WmMaterialDto;
import com.iSchool.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 素材上传
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    /**
     * 素材列表查询
     * @param wmMaterialDto 继承自PageResult包含page数，pageSize,以及是否收藏的信息
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findList(WmMaterialDto wmMaterialDto){
        return wmMaterialService.findList(wmMaterialDto);
    }

    /**
     * 添加收藏
     * @param id
     * @return
     */
    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable Integer id){
        return wmMaterialService.collect(id);
    }

    /**
     * 取消收藏
     * @param id
     * @return
     */
    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollect(@PathVariable Integer id){
        return wmMaterialService.cancelCollect(id);
    }

    /**
     * 删除图片
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult delPicture(@PathVariable Integer id){
        return wmMaterialService.delPicture(id);
    }
}
