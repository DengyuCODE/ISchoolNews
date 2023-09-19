package com.iSchool.article.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iSchool.article.service.ApArticleService;
import com.iSchool.article.service.ArticleFreemarkerService;
import com.iSchool.common.constants.ArticleConstants;
import com.iSchool.file.service.FileStorageService;
import com.iSchool.model.article.pojos.ApArticle;
import com.iSchool.model.search.dtos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Async
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;

    /**
     * 生成文章静态页面
     * @param article
     * @param content
     */
    @Override
    public void buildArticleToMinIO(ApArticle article, String content) {
        try{
            //文章内容通过freemarker生成html文件
            Template template = configuration.getTemplate("article.ftl");
            //传入参数
            Map<String,Object> params = new HashMap<>();
            params.put("content", JSON.parseArray(content));
            StringWriter out=new StringWriter();
            //合成
            //第一个参数 数据模型
            //第二个参数  输出流(将数据写入字符缓冲区)
            template.process(params,out);

            //把文件上传到minio
            //ByteArrayInputStream中有缓冲区用于存放用于read()的缓冲数据
            String path =
                    fileStorageService.uploadHtmlFile("", article.getId() + "html", new ByteArrayInputStream(out.toString().getBytes()));

            //4修改ap_article表，保存static_url字段
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId,article.getId()).set(ApArticle::getStaticUrl,path));

            //通过kafka将文章保存到es
            CreateArticleEsIndex(article,content,path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 为新的文章创建es索引
     * @param article
     * @param content
     * @param path
     */
    private void CreateArticleEsIndex(ApArticle article, String content, String path) {
        SearchArticleVo searchArticleVo=new SearchArticleVo();
        BeanUtils.copyProperties(article,searchArticleVo);
        searchArticleVo.setContent(content);
        searchArticleVo.setStaticUrl(path);
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC,JSON.toJSONString(searchArticleVo));
    }
}
