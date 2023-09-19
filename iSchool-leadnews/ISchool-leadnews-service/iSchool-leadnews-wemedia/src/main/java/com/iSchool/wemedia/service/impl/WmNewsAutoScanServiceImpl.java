package com.iSchool.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iSchool.apis.article.IArticleClient;
import com.iSchool.common.aliyun.GreenImageScan;
import com.iSchool.common.aliyun.GreenTextScan;
import com.iSchool.common.tess4j.Tess4jClient;
import com.iSchool.file.service.FileStorageService;
import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.pojos.WmChannel;
import com.iSchool.model.wemedia.pojos.WmNews;
import com.iSchool.model.wemedia.pojos.WmSensitive;
import com.iSchool.model.wemedia.pojos.WmUser;
import com.iSchool.utils.common.SensitiveWordUtil;
import com.iSchool.wemedia.mapper.WmChannelMapper;
import com.iSchool.wemedia.mapper.WmNewsMapper;
import com.iSchool.wemedia.mapper.WmSensitiveMapper;
import com.iSchool.wemedia.mapper.WmUserMapper;
import com.iSchool.wemedia.service.WmNewsAutoScanService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;

    /**
     * 自媒体文章审核
     * 实现发布文章渝审核文章之间实现异步调用
     * @param id  自媒体文章id
     */
    @Override
    @Async  //标明当前方法是一个异步方法
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null){
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }

        //只有提交状态的文章才可以进行审核
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            //从内容中提取纯文本内容和图片
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);

            //先进行敏感词审核
            handleSensitiveScan((String)textAndImages.get("content"),wmNews);

            //2.审核文本内容  阿里云接口
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"),wmNews);
            if(!isTextScan)return;

            //3.审核图片  阿里云接口
            boolean isImageScan =  handleImageScan((List<String>) textAndImages.get("images"),wmNews);
            if(!isImageScan)return;

            //4.审核成功，保存app端的相关的文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }

            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            //修改文章审核状态
            updateWmNews(wmNews,(short) 9,"审核成功");

        }
    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自管理的敏感词审核
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {

        boolean flag = true;

        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size() >0){
            updateWmNews(wmNews,(short) 2,"当前文章中存在违规内容"+map);
            flag = false;
        }

        return flag;
    }


    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;
    /**
     * 远程调用，生成app端文章
     * @param wmNews
     * @return
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        //生成一个ArticleDto
        ArticleDto articleDto =new ArticleDto();
        //属性拷贝
        BeanUtils.copyProperties(wmNews,articleDto);

        //属性填充
        //文章布局
        articleDto.setLayout(wmNews.getType());

        //文章频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel != null){
            articleDto.setChannelName(wmChannel.getName());
        }

        //作者id
        articleDto.setAuthorId(wmNews.getUserId().longValue());
        //作者名称
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser != null){
            articleDto.setAuthorName(wmUser.getName());
        }

        //设置文章id
        if(wmNews.getArticleId() != null){
            articleDto.setId(wmNews.getArticleId());
        }
        articleDto.setCreatedTime(new Date());

        //远程调用保存文章
        ResponseResult responseResult = articleClient.saveArticle(articleDto);
        return responseResult;
    }

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private Tess4jClient tess4jClient;
    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        //判断文章中是否有图片
        if(images == null){
            return flag;
        }

        //先对图片进行去重
        images = images.stream().distinct().collect(Collectors.toList());
        //从minio中下载图片
        //并且完成图片文字的审核
        List<byte[]> imageList=new ArrayList<>();
        try{
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);
                //图片识别文字审核---begin-----

                //从byte[]转换为butteredImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage imageFile = ImageIO.read(in);
                //识别图片的文字
                String result = null;

                //审核是否包含自管理的敏感词
                boolean isSensitive = handleSensitiveScan(result, wmNews);
                if(!isSensitive){
                    return isSensitive;
                }

                //图片识别文字审核---end-----
                imageList.add(bytes);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //审核图片
        try {
            Map map = greenImageScan.imageScan(imageList);
            //审核失败
            if(map.get("suggestion").equals("block")){
                flag = false;
                updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
            }
            //不确定信息  需要人工审核
            if(map.get("suggestion").equals("review")){
                flag = false;
                updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Autowired
    private GreenTextScan greenTextScan;
    /**
     * 审核文本内容
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;

        //极端因素(标题内容都为空)
        if((wmNews.getTitle()+"-"+content).length() == 0){
            return flag;
        }

        try {
            Map map = greenTextScan.greeTextScan((wmNews.getTitle()+"-"+content));
            if(map != null){
                //审核失败
                if(map.get("suggestion").equals("block")){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }

                //不确定信息  需要人工审核
                if(map.get("suggestion").equals("review")){
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * 修改文章的审核状态和reason(操作成功或者审核失败原因)
     * @param wmNews
     * @param status  审核状态
     * @param reason 审核成功或者失败原因
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 获取文章内容中的文本和图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        StringBuilder text=new StringBuilder();
        List<String> images=new ArrayList<>();
        //如果文章内容不为空的情况下
        String content = wmNews.getContent();
        if(StringUtils.isNotBlank(content)){
            List<Map> maps = JSON.parseArray(content, Map.class);
            //将图片和文本翻看
            for (Map map : maps) {
                if (map.get("type").equals("text")){
                    text.append(map.get("value"));
                }
                if(map.get("type").equals("image")){
                    images.add((String)map.get("value"));
                }
            }
        }
        //提取文章封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        //存入Map中返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content",text.toString());
        resultMap.put("images",images);
        return resultMap;

    }
}
