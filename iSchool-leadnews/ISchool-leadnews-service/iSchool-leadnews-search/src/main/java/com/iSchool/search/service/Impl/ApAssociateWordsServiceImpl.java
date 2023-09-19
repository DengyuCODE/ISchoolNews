package com.iSchool.search.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.search.dtos.UserSearchDto;
import com.iSchool.model.search.pojos.ApAssociateWords;
///import com.iSchool.search.mapper.ApAssociateWordsMapper;
import com.iSchool.search.service.ApAssociateWordsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 联想词表 服务实现类
 * </p>
 *
 * @author iSchool
 */
@Slf4j
@Service
public class ApAssociateWordsServiceImpl  implements ApAssociateWordsService {
        @Autowired
        MongoTemplate mongoTemplate;

        /**
         * 联想词
         * @param userSearchDto
         * @return
         */
        @Override
        public ResponseResult findAssociate(UserSearchDto userSearchDto) {
            //1 参数检查
            if(userSearchDto == null || StringUtils.isBlank(userSearchDto.getSearchWords())){
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
            }
            //分页检查
            if (userSearchDto.getPageSize() > 20) {
                userSearchDto.setPageSize(20);
            }

            //3 执行查询 模糊查询
            Query query = Query.query(Criteria.where("associateWords").regex(".*?\\" + userSearchDto.getSearchWords() + ".*"));
            query.limit(userSearchDto.getPageSize());
            List<ApAssociateWords> wordsList = mongoTemplate.find(query, ApAssociateWords.class);

            return ResponseResult.okResult(wordsList);
        }
}