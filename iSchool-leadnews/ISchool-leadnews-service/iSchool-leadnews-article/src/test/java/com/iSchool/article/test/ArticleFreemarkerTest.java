package com.iSchool.article.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iSchool.article.ArticleApplication;
import com.iSchool.article.mapper.ApArticleContentMapper;
import com.iSchool.article.mapper.ApArticleMapper;
import com.iSchool.article.service.ApArticleService;
import com.iSchool.file.service.FileStorageService;
import com.iSchool.model.article.pojos.ApArticle;
import com.iSchool.model.article.pojos.ApArticleContent;
import com.iSchool.article.ArticleApplication;
import com.iSchool.article.mapper.ApArticleContentMapper;
import com.iSchool.article.service.ApArticleService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

//"13028623871241256981302862387124125698L"
@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class ArticleFreemarkerTest {
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;
    //创建静态的url测试方法
    public void creatStaticUrlTest() throws  Exception{
        //1获取文章内容(通过文章id获取)
        ApArticleContent apArticleContent= apArticleContentMapper
                .selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                        .eq(ApArticleContent::getArticleId, 1302862387124125698L));

        //如果文件id是否存在存在并且文章内容是否为空
        if(apArticleContent.getArticleId()!=null && apArticleContent.getContent()!=null){
            //2文章内容通过freemarker生成html文件
             //2.1获取模板对象
            Template template = configuration.getTemplate("article.ftl");
             //2.2传入参数
            Map<String,Object> param = new HashMap<>();
            param.put("content", JSONArray.parseArray(apArticleContent.getContent()));
            log.info("!!!!!{}",JSONArray.parseArray(apArticleContent.getContent()));
            StringWriter out=new StringWriter();
            //合成
            //第一个参数 数据模型
            //第二个参数  输出流(将数据写入字符缓冲区)
            template.process(param,out);
            //ByteArrayInputStream中有缓冲区用于存放用于read()的缓冲数据
            InputStream in = new ByteArrayInputStream(out.toString().getBytes());

            //3把html文件上传到minio中
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", in);

            //4修改ap_article表，保存static_url字段
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId,apArticleContent.getArticleId())
                    .set(ApArticle::getStaticUrl,path));
        }

    }
}
