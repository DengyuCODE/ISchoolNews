package com.iSchool.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.file.service.FileStorageService;
import com.iSchool.model.common.dtos.PageResponseResult;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.wemedia.dtos.WmMaterialDto;
import com.iSchool.model.wemedia.pojos.WmMaterial;
import com.iSchool.model.wemedia.pojos.WmNewsMaterial;
import com.iSchool.utils.thread.WmThreadLocalUtil;
import com.iSchool.wemedia.mapper.WmMaterialMapper;
import com.iSchool.wemedia.mapper.WmNewsMaterialMapper;
import com.iSchool.wemedia.service.WmMaterialService;
import com.iSchool.wemedia.mapper.WmMaterialMapper;
import com.iSchool.wemedia.mapper.WmNewsMaterialMapper;
import com.iSchool.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 图片素材上传
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.检查参数
        if(multipartFile == null && multipartFile.getSize()==0 ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);//无效参数
        }
        //2.上传图片到minio
        //获取源图片名称
        String originalFilename = multipartFile.getOriginalFilename();
        //使用UUID生成文件名
        String fileName= UUID.randomUUID().toString().replace("-","");
        //拼接文件名前后缀
        String fileId = null;
        String postfix=originalFilename.substring(originalFilename.lastIndexOf("."));
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}",fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //3.上传图片到数据库
        WmMaterial wmMaterial=new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short)0); //0未收藏 1 收藏
        wmMaterial.setType((short)0); //0 图片 1视频
        wmMaterial.setCreatedTime(new Date());
        this.save(wmMaterial);
        //4.响应结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 获取图片素材列表
     * @param wmMaterialDto
     * @return
     */
    @Override

    public ResponseResult findList(WmMaterialDto wmMaterialDto) {
        Integer id = WmThreadLocalUtil.getUser().getId();
        System.out.println("用户id为:{}"+WmThreadLocalUtil.getUser().getId());
        //检查参数信息(参数为空初始化)
        wmMaterialDto.checkParam();

        //分页查询
        Page page=new Page(wmMaterialDto.getPage(),wmMaterialDto.getSize());
        LambdaQueryWrapper<WmMaterial> queryWrapper=new LambdaQueryWrapper<>();
        //是否收藏
        if(wmMaterialDto.getIsCollection() != null && wmMaterialDto.getIsCollection()==1){
            queryWrapper.eq(WmMaterial::getIsCollection,wmMaterialDto.getIsCollection());
        }

        //根据用户查询
        log.info("用户id为:{}",WmThreadLocalUtil.getUser().getId());
        queryWrapper.eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser().getId());

        //以创建时间排序
        queryWrapper.orderByDesc(WmMaterial::getCreatedTime);

        this.page(page,queryWrapper);

        //返回结果
        ResponseResult responseResult = new PageResponseResult(wmMaterialDto.getPage(),wmMaterialDto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 图片收藏
     * @param id
     * @return
     */
    @Override
    public ResponseResult collect(Integer id) {
        //判断id情况
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //查找库中是否有该id
        WmMaterial wmMaterial = getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId, id));
        if(wmMaterial==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //根据id更改素材表信息
        wmMaterial.setIsCollection((short)1);
        updateById(wmMaterial);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 取消收藏
     * @param id
     * @return
     */
    @Override
    public ResponseResult cancelCollect(Integer id) {
        //判断id情况
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);//参数失败
        }
        //查找库中是否有该id
        WmMaterial wmMaterial = getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId, id));
        if(wmMaterial==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //根据id更改素材表信息
        wmMaterial.setIsCollection((short)0);
        updateById(wmMaterial);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除图片
     * @param id
     * @return
     */
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Override
    public ResponseResult delPicture(Integer id) {
        //检查参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //根据参数获取图片
        WmMaterial wmMaterial = getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId, id));
        if(wmMaterial==null){
            //图片不存在
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);//1002 数据不存在
        }
        //数据存在
        //图片是否被引用(在文章素材表中)
        this.list();
        List<WmNewsMaterial> wmNewsMaterials = wmNewsMaterialMapper.selectList(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getMaterialId, wmMaterial.getId()));
        if(wmNewsMaterials!=null && wmNewsMaterials.size()>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID.getCode(),"文件删除失败");
        }
        this.removeById(wmMaterial.getId());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
