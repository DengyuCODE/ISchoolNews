package com.iSchool.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.search.dtos.UserSearchDto;
import com.iSchool.model.search.pojos.ApUserSearch;
import com.iSchool.model.user.pojos.ApUser;
//import com.iSchool.search.mapper.ApUserSearchMapper;
import com.iSchool.search.pojos.HistorySearchDto;
import com.iSchool.search.service.ApUserSearchService;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
@Log4j2
public class ApUserSearchServiceImpl  implements ApUserSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 新增搜索记录
     * @param keyword
     * @param userId
     */
    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        //查询当前用户的搜索关键词
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        //判断当前查询历史记录是否存在
        if(apUserSearch != null){
            //当前记录原本就有，直接更改创建时间结束
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return ;
        }else {
            //表示当前为新的记录进一步判断
            apUserSearch =new ApUserSearch();
            apUserSearch.setUserId(userId);
            apUserSearch.setKeyword(keyword);
            apUserSearch.setCreatedTime(new Date());
            //判断当前历史记录条数
            Query query1 = Query.query(Criteria.where("userId").is(userId));//查询出当前用户的所有历史搜索信息
            query1.with(Sort.by(Sort.Direction.DESC,"createdTime"));//根据创建时间降序排序
            List<ApUserSearch> apUserSearchList = mongoTemplate.find(query1, ApUserSearch.class);
            if(apUserSearchList == null || apUserSearchList.size() < 10){
                //直接加入
                mongoTemplate.save(apUserSearch);
            }else {
                //删除最早创建的那条记录
//                mongoTemplate.remove(apUserSearchList.get(apUserSearchList.size()-1));
//                mongoTemplate.save(apUserSearch);
                ApUserSearch lastUserSearch = apUserSearchList.get(apUserSearchList.size() - 1);
                mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(lastUserSearch.getId())),apUserSearch);
            }
        }
    }

    /**
     * 获取搜索记录列表
     */
    @Override
    public ResponseResult findUserSearch() {
        //获取用户id
        ApUser user = ApThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        } else{
            Integer userId = user.getId();
            Query query = Query.query(Criteria.where("userIs").is(userId)).with(Sort.by(Sort.Direction.DESC,"createdTime"));
            List<ApUserSearch> apUserSearchList = mongoTemplate.find(query, ApUserSearch.class);
                return ResponseResult.okResult(apUserSearchList);
        }
    }

    @Override
    public ResponseResult deleteUserSearch(HistorySearchDto dto) {
        if(dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //获取用户id
        ApUser user = ApThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Query query = Query.query(Criteria.where("id").is(dto.getId()).and("userId").is(user.getId()));
        mongoTemplate.remove(query,ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}