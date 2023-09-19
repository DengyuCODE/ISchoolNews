package com.iSchool.apis.user;

import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.user.pojos.ApUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "leadnews-user")
public interface IUserClient {
    /**
     * 获取用户完整信息
     * @param userId
     * @return
     */
    @GetMapping("/api/v1/user")
    public ApUser queryUserInfo(@RequestParam("userId") Integer userId);

    /**
     * 更新用户积分
     * @param user
     */
    @PostMapping("/api/v1/user/update")
    public void updateUser(@RequestBody ApUser user);
}
