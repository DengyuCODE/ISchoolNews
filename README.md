# i校新闻
一款定位在校园内的资讯交流软件，用户可以分享周边新闻或者生活中的点点乐趣。
## 项目功能
项目目前实现了用户登录、文章的延时发布、定时及实时热点资讯更新、阅读文章赚取积分、用户点赞交互、内容审核、文章搜索、等功能，目前还在进行功能拓展，希望能够完善出更具商业价值的功能。
## 项目模块
该项目基于SpringCloud Alibaba实现微服务架构，主要包括网关服务、用户服务、app端服务、用户行为服务、文章发布服务、文章搜索服务，后序计划继续拓展。
## 主要技术
|技术|说明|
|:----|:----|
|SpringBoot |MVC框架 |
|SpringCloud |微服务框架 |
|Mybatis-Plus |数据库框架 |
|GateWay |网关服务 |
|Fegin |远程调用服务 |
|Nacos |服务注册、配置 |
|Redis |缓存中间件 |
|RabbitMQ |消息中间件 |
|xxl-job |定时任务框架 |
|Kafka |做实时流式处理 |
|Minio |静态化页面存储 |
## 项目结构
```
├─iSchool-leadNews-basic├─iSchool-file-starter：Minio模块
├─iSchool-leadNews-common: 公共模块
├─iSchool-leadNews-feign-api: 远程调用抽取模块
├─iSchool-leadNews-gateway: 网关模块
├─iSchool-leadNews-model:类定义模块
├─iSchool-leadNews-service:服务功能模块
│            ├─iSchool-leadNews-article：文章服务模块模块
│            ├─iSchool-leadNews-behavior：用户行为模块
│            ├─iSchool-leadNews-schedule：任务延迟发布模块
│            ├─iSchool-leadNews-search：文章搜索模块
│            ├─iSchool-leadNews-user：用户服务模块
│            └─iSchool-leadNews-wemedia：媒体端模块
└─iSchool-leadNews-utils：通用工具模块
```
