package com.iSchool.file.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
public class MinIOConfigProperties implements Serializable {

    private String accessKey;//账户
    private String secretKey;//密码
    private String bucket;//桶
    private String endpoint;//连接路径
    private String readPath;//静态页面的路径
}
