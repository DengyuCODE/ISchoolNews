package com.iSchool.search.listener;


import com.alibaba.fastjson.JSON;
import com.iSchool.common.constants.ArticleConstants;
import com.iSchool.model.search.dtos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 接收消息为文章创建索引
     * @param message
     */
    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
    public void onMessage(String message){
        //校验参数
        if(StringUtils.isNotBlank(message)){
            log.info("SyncArticleListener,message={}",message);

            //先将传过来的数据转换成SearchArticleVo对象
            SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);

            IndexRequest indexRequest =new IndexRequest("app_info_article");
            indexRequest.id(searchArticleVo.getId().toString());
            indexRequest.source(message, XContentType.JSON);

            try {
                restHighLevelClient.index(indexRequest,RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("sync es error={}",e);
            }
        }


    }
}
