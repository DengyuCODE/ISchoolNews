package com.iSchool.apis.article.fallback;

import com.iSchool.apis.article.IArticleClient;
import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.apis.article.IArticleClient;
import org.springframework.stereotype.Component;

/**
 * 服务降级处理
 */
@Component
public class IArticleClientFallback implements IArticleClient {
    /**
     * 保存文章服务交际
     * - 服务降级是服务自我保护的一种方式，或者保护下游服务的一种方式，用于确保服务不会受请求突增影响变得不可用，确保服务不会崩溃
     * - 服务降级虽然会导致请求失败，但是不会导致阻塞。
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败");
    }
}
