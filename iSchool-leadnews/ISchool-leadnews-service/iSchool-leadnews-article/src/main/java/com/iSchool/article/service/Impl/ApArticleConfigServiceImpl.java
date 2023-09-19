package com.iSchool.article.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.article.mapper.ApArticleConfigMapper;
import com.iSchool.article.service.ApArticleConfigService;
import com.iSchool.model.article.pojos.ApArticleConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {
    /**
     * 根据传过来的map修根文章配置表
     * @param map key:articleId   key:enable
     */
    @Override
    public void updateByMap(Map map) {
        Long articleId= (Long) map.get("articleId");
        Short enable = (short) map.get("enable");
        ApArticleConfig articleConfig = getOne(Wrappers.<ApArticleConfig>lambdaQuery().eq(ApArticleConfig::getArticleId, articleId));
        boolean isDown = false;
        if(enable.equals(0)){
            isDown = true;
        }
        articleConfig.setIsDown(isDown);
        updateById(articleConfig);
    }
}
