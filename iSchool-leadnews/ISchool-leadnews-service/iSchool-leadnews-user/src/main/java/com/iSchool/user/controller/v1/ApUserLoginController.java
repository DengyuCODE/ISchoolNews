package com.iSchool.user.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.user.dtos.LoginDto;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/login")
/**
 * app端用户登录
 */
public class ApUserLoginController {

    @Autowired
    private ApUserService apUserService;

    /**
     * 用户登录
     * @param dto
     * @return
     */
    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto dto){
        return apUserService.login(dto);
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody ApUser user){
        return apUserService.register(user);
    }
}
