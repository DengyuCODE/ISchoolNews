package com.iSchool.article.listener;

import com.alibaba.fastjson.JSON;
import com.iSchool.article.service.ApArticleConfigService;
import com.iSchool.common.constants.WmNewsMessageConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ArticleUpOrDownListener {
    //主要去修改文章配置表中的enable
    @Autowired
    private ApArticleConfigService apArticleConfigService;

    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void getUpOrDownArticleMessage(String message){
        if(StringUtils.isNotBlank(message)){
            //因为传过来的是json字符串(在接收的时候完成了反序列化)
            Map map = JSON.parseObject(message, Map.class);
            apArticleConfigService.updateByMap(map);
        }
    }
}
