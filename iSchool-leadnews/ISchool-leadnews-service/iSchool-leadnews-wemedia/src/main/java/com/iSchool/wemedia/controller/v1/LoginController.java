package com.iSchool.wemedia.controller.v1;

import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.wemedia.dtos.WmLoginDto;
import com.iSchool.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private WmUserService wmUserService;

    //自媒体页面的登录
    @PostMapping("/in")
    public ResponseResult login(@RequestBody WmLoginDto dto){
        return wmUserService.login(dto);
    }

}
