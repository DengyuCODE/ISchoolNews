package com.iSchool.article.config;

import com.iSchool.common.constants.ArticleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class RabbitMQConfig implements ApplicationContextAware {
// 连接工厂配置

    @Bean
    public ThreadPoolTaskExecutor rabbitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);               // 核心线程数
        executor.setMaxPoolSize(20);                // 最大线程数
        executor.setKeepAliveSeconds(60);           // 线程最大空闲时间（秒）
        executor.setQueueCapacity(1000);            // 线程池队列容量
        executor.setThreadNamePrefix("RabbitMQ-");  // 线程名前缀
        executor.initialize();
        return executor;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                         ThreadPoolTaskExecutor rabbitExecutor) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setTaskExecutor(rabbitExecutor);
        return rabbitTemplate;
    }

    //生产者确认机制，定义return回调
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate对象
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 配置ReturnCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // 判断是否是延迟消息
            Integer receivedDelay = message.getMessageProperties().getReceivedDelay();
            if (receivedDelay != null && receivedDelay > 0) {
                // 是一个延迟消息，忽略这个错误提示
                return;
            }
            // 记录日志
            log.error("消息发送到队列失败，响应码：{}, 失败原因：{}, 交换机: {}, 路由key：{}, 消息: {}",
                    replyCode, replyText, exchange, routingKey, message.toString());
            // 如果有需要的话，重发消息
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        });
    }

    //定义ttl交换机
    @Bean
    public TopicExchange ttlExchange(){
        return new TopicExchange(ArticleConstants.TTL_EXCHANGE);
    }

    //定义ttl队列
    @Bean
    public Queue ttlQueue(){
        return QueueBuilder.durable(ArticleConstants.TTL_QUEUE) //持久化队列
                .ttl(15000)//指定超时时间15秒
                .deadLetterExchange(ArticleConstants.DLE_TTL_EXCHANGE)//指定死信交换机
                .deadLetterRoutingKey("dle.point")//设置死信交换机的路由
                .maxLength(1000) //设置队列最大消息数量为1000
                .build();
    }

    //定义死信交换机
    @Bean
    public TopicExchange dlExchange(){
        return new TopicExchange(ArticleConstants.DLE_TTL_EXCHANGE);
    }

    //定义死信队列
    @Bean
    public  Queue dlQueue(){
        return QueueBuilder.durable(ArticleConstants.DLE_TTL_QUEUE).build();
    }

    //绑定交换机
    @Bean
    public Binding ttlBinding(){
        return BindingBuilder.bind(ttlQueue()).to(ttlExchange()).with(ArticleConstants.TTL_ROUTINGKEY);
    }

    //绑定死信交换机与队列
    @Bean
    public Binding dlTtlBinding(){
        return BindingBuilder.bind(dlQueue()).to(dlExchange()).with(ArticleConstants.DLE_ROUTINGKEY);
    }
}
