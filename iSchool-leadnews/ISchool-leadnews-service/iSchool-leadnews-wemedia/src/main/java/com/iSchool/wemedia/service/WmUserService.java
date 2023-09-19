package com.iSchool.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.dtos.WmLoginDto;
import com.iSchool.model.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmLoginDto dto);

    public ResponseResult register(WmUser wmUser);

}