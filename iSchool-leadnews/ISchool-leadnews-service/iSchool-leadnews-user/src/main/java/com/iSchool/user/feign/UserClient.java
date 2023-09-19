package com.iSchool.user.feign;

import com.iSchool.apis.user.IUserClient;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserClient implements IUserClient {
    @Autowired
    private ApUserService userService;

    /**
     * 获取用户完整信息
     * @param userId
     * @return
     */
    @PostMapping("/api/v1/user")
    public ApUser queryUserInfo(@RequestParam("userId") Integer userId){
        ApUser user = userService.getById(userId);
        return user;
    }

    /**
     * 更新用户数据
     * @param user
     */
    @PostMapping("/api/v1/user/update")
    public void updateUser(@RequestBody ApUser user){
        userService.updateById(user);
    }
}
