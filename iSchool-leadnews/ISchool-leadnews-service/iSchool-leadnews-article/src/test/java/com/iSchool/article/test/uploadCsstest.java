package com.iSchool.article.test;

import com.iSchool.article.ArticleApplication;
import com.iSchool.file.service.FileStorageService;
import com.iSchool.article.ArticleApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class uploadCsstest {
    @Autowired
    private FileStorageService fileStorageService;
    //上传index.css
    @Test
    public void cssUpload() throws Exception{
        FileInputStream fileInputStream
                =new FileInputStream("D:\\Freemarker(模板)\\模板文件\\plugins\\css\\index.css");
        //自建立连接
        MinioClient minioClient=MinioClient.builder()
                .credentials("minio","minio123")
                .endpoint("http://192.168.200.130:9000")
                .build();
        //上传
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object("plugins/css/index.css")//文件名
                .contentType("text/css")//文件类型
                .bucket("leadnews")//存放的桶的名称
                .stream(fileInputStream,fileInputStream.available(),-1)
                .build();
        minioClient.putObject(putObjectArgs);
    }
    @Test
    public void jsUpload() throws Exception{
        FileInputStream fileInputStream
                =new FileInputStream("D:\\Freemarker(模板)\\模板文件\\plugins\\js\\index.js");
        //自建立连接
        MinioClient minioClient=MinioClient.builder()
                .credentials("minio","minio123")
                .endpoint("http://192.168.200.130:9000")
                .build();
        //上传
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object("plugins/js/index.js")//文件名
                .contentType("text/js")//文件类型
                .bucket("leadnews")//存放的桶的名称
                .stream(fileInputStream,fileInputStream.available(),-1)
                .build();
        minioClient.putObject(putObjectArgs);
    }
}
