package com.iSchool.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.search.dtos.UserSearchDto;
import com.iSchool.model.search.pojos.ApAssociateWords;

/**
 * <p>
 * 联想词表 服务类
 * </p>
 *
 * @author itiSchool
 */
public interface ApAssociateWordsService {

    /**
     联想词
     @param userSearchDto
     @return
     */
    ResponseResult findAssociate(UserSearchDto userSearchDto);
}