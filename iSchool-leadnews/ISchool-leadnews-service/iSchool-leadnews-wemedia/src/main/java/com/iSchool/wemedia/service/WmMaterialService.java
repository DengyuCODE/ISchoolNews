package com.iSchool.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.dtos.WmMaterialDto;
import com.iSchool.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 图片素材上传
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 获取图片素材列表
     * @param wmMaterialDto
     * @return
     */
    public ResponseResult findList(WmMaterialDto wmMaterialDto);

    /**
     * 图片收藏
     * @param id
     * @return
     */
    public ResponseResult collect(Integer id);

    /**
     * 取消收藏
     * @param id
     * @return
     */
    public ResponseResult cancelCollect(Integer id);

    /**
     * 删除图片
     * @param id
     * @return
     */
    public ResponseResult delPicture(Integer id);
}