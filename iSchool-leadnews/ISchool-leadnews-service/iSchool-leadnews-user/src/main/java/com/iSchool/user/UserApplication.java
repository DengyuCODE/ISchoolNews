package com.iSchool.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient//集成注册中心
@MapperScan("com.iSchool.user.mapper")
@Slf4j
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
        log.info("UserApplication启动成功");
    }
}
