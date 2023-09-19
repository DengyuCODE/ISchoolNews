package com.iSchool.article.controller.v1;

import com.iSchool.article.service.ApPointService;
import com.iSchool.model.article.pojos.ApPoint;
import com.iSchool.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/point")
public class ApPointController {

    @Autowired
    private ApPointService apPointService;

    /**
     * 加载文章时调用接口开始计时
     * @param apPoints
     * @return
     */
    @PostMapping("/load_article")
    public ResponseResult loadArticle(@RequestBody ApPoint apPoints){
        return apPointService.keepTime(apPoints);
    }

    /**
     * 退出时返回阅读文章的时间长度
     * @param articleId
     * @param readTime
     * @return
     */
    @PostMapping("/read-time")
    public ResponseResult readTime(@RequestParam(value = "articleId") Long articleId,
                                   @RequestParam(value = "readTime") Long readTime,
                                   @RequestParam(value = "flag") Integer flag){
        return apPointService.readTime(articleId,readTime,flag);
    }

}
