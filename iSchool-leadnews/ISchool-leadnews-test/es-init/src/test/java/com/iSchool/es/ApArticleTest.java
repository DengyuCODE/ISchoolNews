package com.iSchool.es;

import com.alibaba.fastjson.JSON;
import com.iSchool.es.mapper.ApArticleMapper;
import com.iSchool.es.pojo.SearchArticleVo;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApArticleTest {

    /**
     * 注意：数据量的导入，如果数据量过大，需要分页导入
     * @throws Exception
     */
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void init() throws Exception {
        //查询数据库数据
        List<SearchArticleVo> searchArticleVos = apArticleMapper.loadArticleList();

        //批量导入es索引库中
        BulkRequest bulkRequest = new BulkRequest("app_info_article");
        for (SearchArticleVo searchArticleVo : searchArticleVos) {
            //根据文章id创建索引id
            IndexRequest indexRequest =new IndexRequest().id(searchArticleVo.getId().toString());
            indexRequest.source(JSON.toJSONString(searchArticleVo), XContentType.JSON);

            //批量添加数据
            bulkRequest.add(indexRequest);
        }

        //执行
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

}