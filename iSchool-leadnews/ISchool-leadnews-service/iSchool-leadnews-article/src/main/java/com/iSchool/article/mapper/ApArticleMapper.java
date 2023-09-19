package com.iSchool.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iSchool.model.article.dtos.ArticleHomeDto;
import com.iSchool.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    /**
     * 加载文章列表
     * @param dto
     * @param type 1 加载更多 2 加载最新
     * @return
     */
    public List<ApArticle> loadArticleList(@Param("dto") ArticleHomeDto dto,@Param("type") Short type);

    public List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date dayParam);
}
