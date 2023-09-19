package com.iSchool.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.search.dtos.UserSearchDto;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.search.service.ApUserSearchService;
import com.iSchool.search.service.ArticleSearchService;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Log4j2
public class ArticleSearchServiceImpl implements ArticleSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ApUserSearchService apUserSearchService;
    /**
     * 用es查询
     * @param userSearchDto
     * @return
     */
    @Override
    public ResponseResult search(UserSearchDto userSearchDto){
        //检查参数(参数为空或者说搜索关键字为空)
        if(userSearchDto == null || StringUtils.isBlank(userSearchDto.getSearchWords())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //获取当前app端的用户
        ApUser user = ApThreadLocalUtil.getUser();
        //异步调用 保存搜索记录
        //解释:要求 userSearchDto.getFromIndex()==0 是因为向下滑动更新页码，不需要在向下滑动的时候再次进行保存操作
        if(user != null && userSearchDto.getFromIndex() == 0){
            apUserSearchService.insert(userSearchDto.getSearchWords(), user.getId());
        }

        //设置查询条件
        SearchRequest searchRequest =new SearchRequest("app_info_article");
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();

        //设置布尔查询
        BoolQueryBuilder boolQueryBuilder =new BoolQueryBuilder();

        //关键字的分词之后查询
        QueryStringQueryBuilder queryStringQueryBuilder =
                QueryBuilders.queryStringQuery(userSearchDto.getSearchWords()).field("title").field("content")
                        .defaultOperator(Operator.OR);  //defaultOperator(Operator.OR):设置分词之后的条件是"或"的关系(可以获取到更多结果集)
        //查询必须带有关键字
        boolQueryBuilder.must(queryStringQueryBuilder);

        //查询时间小于minBehotTime的数据
        RangeQueryBuilder rangeQueryBuilder=
                QueryBuilders.rangeQuery("publishTime").lt(userSearchDto.getMinBehotTime().getTime());
        boolQueryBuilder.filter(rangeQueryBuilder); //将其设置为过滤条件

        //分页查询
        searchSourceBuilder.from(0);//从零开始
        searchSourceBuilder.size(userSearchDto.getPageSize());

        //按照发布时间倒序查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);

        //设置高亮字段 title
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");//设置高亮字段的作用域
        highlightBuilder.preTags("<font style='color: red; font-size: inherit;'>");//高亮前缀
        highlightBuilder.postTags("</font>");//高亮后缀

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        //拿到结果，进行后面的封装
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //结果封装
        //获取命中的结果
        List<Map> list =new ArrayList<>();

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            //将数据以json字符串的形式返回
            String json = hit.getSourceAsString();
            //将字符串转换成map
            Map map = JSON.parseObject(json, Map.class);
            //处理高亮字段
            if(hit.getHighlightFields() != null && hit.getHighlightFields().size() > 0){
                //获取高亮字段
                Text[] titles = hit.getHighlightFields().get("title").getFragments();//getFragments() 获取高亮碎片，标题中可能出现多处高亮
                //转换成字符串
                String title = StringUtils.join(titles);
                //高亮标题
                map.put("h_title",title);
            }else{
                //原始标题
                map.put("h_title",map.get("title"));
            }
            list.add(map);
        }

        return ResponseResult.okResult(list);
    }
}
