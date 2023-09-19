package com.iSchool.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.user.dtos.LoginDto;
import com.iSchool.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {
    /**
     * app端登录功能
     * @param dto
     * @return
     */
    public ResponseResult login(LoginDto dto);

    /**
     * 用户注册
     * @param user
     * @return
     */
    public ResponseResult register(ApUser user);
}
