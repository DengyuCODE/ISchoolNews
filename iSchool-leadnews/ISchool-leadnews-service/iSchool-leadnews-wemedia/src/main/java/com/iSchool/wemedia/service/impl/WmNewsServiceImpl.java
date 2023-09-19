package com.iSchool.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.common.constants.WemediaConstants;
import com.iSchool.common.constants.WmNewsMessageConstants;
import com.iSchool.common.exception.CustomException;
import com.iSchool.model.common.dtos.PageResponseResult;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.wemedia.dtos.WmNewsDto;
import com.iSchool.model.wemedia.dtos.WmNewsPageReqDto;
import com.iSchool.model.wemedia.pojos.WmMaterial;
import com.iSchool.model.wemedia.pojos.WmNews;
import com.iSchool.model.wemedia.pojos.WmNewsMaterial;
import com.iSchool.model.wemedia.pojos.WmUser;
import com.iSchool.utils.common.SensitiveWordUtil;
import com.iSchool.utils.thread.WmThreadLocalUtil;
import com.iSchool.wemedia.mapper.WmNewsMapper;
import com.iSchool.wemedia.mapper.WmNewsMaterialMapper;
import com.iSchool.wemedia.service.WmMaterialService;
import com.iSchool.wemedia.service.WmNewsAutoScanService;
import com.iSchool.wemedia.service.WmNewsService;
import com.iSchool.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 查询文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {

        //1.检查参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数检查
        dto.checkParam();
        //获取当前登录人的信息
        WmUser user = WmThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //2.分页条件查询
        IPage page = new Page(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //状态精确查询
        if(dto.getStatus() != null){
            lambdaQueryWrapper.eq(WmNews::getStatus,dto.getStatus());
        }

        //频道精确查询
        if(dto.getChannelId() != null){
            lambdaQueryWrapper.eq(WmNews::getChannelId,dto.getChannelId());
        }

        //时间范围查询
        if(dto.getBeginPubDate()!=null && dto.getEndPubDate()!=null){
            lambdaQueryWrapper.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }

        //关键字模糊查询
        if(StringUtils.isNotBlank(dto.getKeyword())){
            lambdaQueryWrapper.like(WmNews::getTitle,dto.getKeyword());
        }

        //查询当前登录用户的文章
        lambdaQueryWrapper.eq(WmNews::getUserId,user.getId());

        //发布时间倒序查询
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);

        page = page(page,lambdaQueryWrapper);

        //3.结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }


    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;
    /**
     * 发布文章或者修改保存为草稿
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        //0.条件判断
        //传入的参数为空或者，里面没有内容
        if(dto == null || dto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.保存或修改文章
        //接收dto中的参数
        WmNews wmNews=new WmNews();
        //属性拷贝，属性名词和类型相同才拷贝
        BeanUtils.copyProperties(dto,wmNews);
        //填充封面图片(dto中是list,WmNews中是String用逗号隔开的)
        if(dto.getImages()!=null && dto.getImages().size()>0){
            //转换为String
            String images = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(images);
        }
        //判断封面类型如果为自动,将文章的type设置为null,后序判断情况
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            wmNews.setType(null);
        }
        //保存或修改
        saveOrUpdateWmNews(wmNews);

        //2.判断是否为草稿，如果为草稿结束当前方法
        if(wmNews.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        //3.不是草稿，保存文章内容图片与素材的关系
        //获取文章内容中的图片
        List<String> materials = ectractUrlInfo(wmNews.getContent());
        //处理文章内容图片与素材的关系
        saveRelativeInfoForContent(wmNews.getId(),materials);

        //4.不是草稿，保存文章封面图片与素材的关系
        //获取文章封面图片
        String images = wmNews.getImages();
        saveRelativeInfoForCover(dto,wmNews,materials);

        //发布文章
        //wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 保存或修改
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews){
        //填充WmNewsDto中没有的属性如用户id，创建时间等等
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);//默认上架

        if(wmNews.getId() == null){
            //保存
            save(wmNews);
        }else {
            //修改
            //删除文章图片与素材的关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }
    }

    /**
     * 将文章内容中的图片转换成图片列表返回
     * @param content
     * @return
     */
    private List<String> ectractUrlInfo(String content){
        List<String> images =new ArrayList<>();
        //获取内容中的数据，根据type,value的关系转换成map类型存储
        // "content":"[
        //    {
        //        "type":"text",
        //        "value":"随着智能手机的普及"
        //    },
        //    {
        //        "type":"image",
        //        "value":"http://192.168.200.130/group1/M00/00/00/wKjIgl5swbGATaSAAAEPfZfx6Iw790.png"
        //    }
        //]"
        List<Map> maps = JSON.parseArray(content,Map.class);
        //遍历maps
        for (Map map : maps) {
            if(map.get("type").equals("image")){
                images.add((String)map.get("value"));
            }
        }
        return images;
    }

    /**
     * 处理文章内容图片与素材的关系
     * @param newsId
     * @param materials
     */
    private void saveRelativeInfoForContent(Integer newsId,List<String> materials) {
        saveRelativeInfo(materials,newsId,WemediaConstants.WM_CONTENT_REFERENCE);
    }

    //保存文章封面图片与素材的关系
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials){
        //获取封面图片
        List<String> images = dto.getImages();

        //如果当前封面类型为自动，则设置封面类型的数据
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            //多图
            if(materials.size() >= 3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            }else if(materials.size() >= 1 && materials.size() < 3){
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            }else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            //修改文章
            if(images != null && images.size() > 0){
                wmNews.setImages(StringUtils.join(images,","));
            }
            updateById(wmNews);
        }
        if(images != null && images.size() > 0){
            saveRelativeInfo(images,wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 保存文章图片与素材的关系到数据库中
     * @param materials
     * @param newsId
     * @param type
     */
    @Autowired
    private WmMaterialService wmMaterialService;

    private void saveRelativeInfo(List<String> materials,Integer newsId,Short type){
        //校验参数
        if(materials !=null && !materials.isEmpty()){
            //先获取素材的id
//            List<Integer> materialIds =new ArrayList<>();
//            for (String material : materials) {
//                WmMaterial wmMaterial =wmMaterialService.getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getUrl,material));
//                if(wmMaterial==null){
//                    //返回异常
//                }
//                materialIds.add(wmMaterial.getId());
//            }
            ////通过图片的url查询素材的id
            List<WmMaterial> dbMaterials = wmMaterialService.list(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl,materials));
            //查询素材情况
            // 没有查出相应素材，手动抛出异常
            if(dbMaterials==null && dbMaterials.size()==0){

                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            //存在有素材未成功查询到
            if(materials.size() != dbMaterials.size()){
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            List<Integer> materialIds=dbMaterials.stream().map((item)->{
                Integer id = item.getId();
                return id;
            }).collect(Collectors.toList());
            //批量保存
            wmNewsMaterialMapper.saveRelations(materialIds,newsId,type);
        }
    }


    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    /**
     * 文章上下架
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        //参数校验
        //1.检查参数
        if(dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID.getCode(),"文章id不可少");
        }
        //2.查询文章
        WmNews wmNews = getById(dto.getId());
        //判断文章是否存在
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.判断文章是否已经发布
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前文章不是发布状态，不能上下架");
        }
        //4.修改上下架属性enable
        if(dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2){
            update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable,dto.getEnable())
                    .eq(WmNews::getId,wmNews.getId()));
        }

        //kafka发送消息
        if(wmNews.getArticleId() != null){
            Map<String,Object> map=new HashMap<>();
            map.put("articleId",wmNews.getArticleId());
            map.put("enable",dto.getEnable());
            kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC,JSON.toJSONString(map));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
