package com.iSchool.article.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.article.mapper.ApArticleConfigMapper;
import com.iSchool.article.mapper.ApArticleContentMapper;
import com.iSchool.article.mapper.ApArticleMapper;
import com.iSchool.article.service.ApArticleService;
import com.iSchool.article.service.ArticleFreemarkerService;
import com.iSchool.common.constants.ArticleConstants;
import com.iSchool.common.constants.BehaviorConstants;
import com.iSchool.common.constants.CommonConstans;
import com.iSchool.common.exception.CustomException;
import com.iSchool.common.exception.ExceptionCatch;
import com.iSchool.common.redis.CacheService;
import com.iSchool.model.article.dtos.ArticleDto;
import com.iSchool.model.article.dtos.ArticleHomeDto;
import com.iSchool.model.article.dtos.ArticleInfoDto;
import com.iSchool.model.article.pojos.ApArticle;
import com.iSchool.model.article.pojos.ApArticleConfig;
import com.iSchool.model.article.pojos.ApArticleContent;
import com.iSchool.model.article.vos.HotArticleVo;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.mess.ArticleVisitStreamMess;
import com.iSchool.model.article.pojos.ApPoint;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    // 单页最大加载的数字
    private final static short MAX_PAGE_SIZE = 50;

    //注入mapper
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 加载文章列表
     * @param dto
     * @param type 1 加载更多 2 加载最新
     * @return
     */
    public ResponseResult load(ArticleHomeDto dto,Short type){
        //1.进行校验
        //校验参数
        //(没有分页参数的情况下默认为10)
        Integer size=dto.getSize();
        if(size == null || size == 0){
            size=10;
        }
        //(保证分页参数的最大值为所给最大值50)
        size=Math.min(size,MAX_PAGE_SIZE);
        //类型参数检验
        if(!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE)&&!type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            type=ArticleConstants.LOADTYPE_LOAD_MORE; //默认加载为加载更多
        }
        //文章频道校验
        if(StringUtils.isEmpty(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);//默认频道为推荐频道
        }
        //校验时间
        if(dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());
        if(dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());
        //2.查询数据
        List<ApArticle> apArticleList=apArticleMapper.loadArticleList(dto,type);
        //3返回封装结果
        return ResponseResult.okResult(apArticleList);
    }

    /**
     * 加载文章列表
     * @param dto
     * @param type      1 加载更多   2 加载最新
     * @param firstPage true  是首页  flase 非首页
     * @return
     */
    @Override
    public ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage) {
        if(firstPage){
            String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if(StringUtils.isNotBlank(jsonStr)){
                List<HotArticleVo> hotArticleVoList = JSON.parseArray(jsonStr, HotArticleVo.class);
                ResponseResult responseResult = ResponseResult.okResult(hotArticleVoList);
                return responseResult;
            }
        }
        return load(dto,type);
    }


    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

   @Autowired
   private ArticleFreemarkerService articleFreemarkerService;

   @Autowired
   private KafkaTemplate kafkaTemplate;

    /**
     * 保存文章(新增、修改)
     * 需要操作文章表，文章内容表，初始化文章配置表
     * @param dto
     * @return`
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto){
        //1.检查参数
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle article=new ApArticle();
        BeanUtils.copyProperties(dto,article);
        //根据id判断文章是否存在(存在 修改，不存在 新增)
        if(dto.getId() == null){
            //文章不存在，新增文章
            this.save(article);

            //初始化文章配置
            ApArticleConfig apArticleConfig=new ApArticleConfig(article.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            //保存文章内容
            ApArticleContent apArticleContent=new ApArticleContent();
            apArticleContent.setArticleId(article.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }else{
            //文章已经存在，修改文章
            //修改文章
            updateById(article);
            //修改文章内容
            ApArticleContent apArticleContent =
                    apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, article.getId()));
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }
        //返回文章id

        //minio保存文章静态页面
        articleFreemarkerService.buildArticleToMinIO(article,dto.getContent());

        return ResponseResult.okResult(article.getId());
    }


    /**
     * 文章数据回显(更新用户行为后更新文章页面状态)
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {

        //0.检查参数
        if (dto == null || dto.getArticleId() == null || dto.getAuthorId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //{ "isfollow": true, "islike": true,"isunlike": false,"iscollection": true }
        boolean isfollow = false, islike = false, isunlike = false, iscollection = false;

        ApUser user = ApThreadLocalUtil.getUser();
        if(user != null){
            //喜欢行为
            String likeBehaviorJson = (String) cacheService.hGet(BehaviorConstants.LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
            if(StringUtils.isNotBlank(likeBehaviorJson)){
                islike = true;
            }
            //不喜欢的行为
            String unLikeBehaviorJson = (String) cacheService.hGet(BehaviorConstants.UN_LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
            if(StringUtils.isNotBlank(unLikeBehaviorJson)){
                isunlike = true;
            }
            //是否收藏
            String collctionJson = (String) cacheService.hGet(BehaviorConstants.COLLECTION_BEHAVIOR+user.getId(),dto.getArticleId().toString());
            if(StringUtils.isNotBlank(collctionJson)){
                iscollection = true;
            }

            //是否关注
            Double score = cacheService.zScore(BehaviorConstants.APUSER_FOLLOW_RELATION + user.getId(), dto.getAuthorId().toString());
            System.out.println(score);
            if(score != null){
                isfollow = true;
            }

        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isfollow", isfollow);
        resultMap.put("islike", islike);
        resultMap.put("isunlike", isunlike);
        resultMap.put("iscollection", iscollection);

        return ResponseResult.okResult(resultMap);
    }



    /**
     * 更新文章的分值  同时更新缓存中的热点文章数据
     * @param mess
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        //1.更新文章的阅读、点赞、收藏、评论的数量
        ApArticle apArticle = updateArticle(mess);
        //2.计算文章的分值
        Integer score = computeScore(apArticle);
        score = score * 3;

        //3.替换当前文章对应频道的热点数据
        replaceDataToRedis(apArticle, score, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());

        //4.替换推荐对应的热点数据
        replaceDataToRedis(apArticle, score, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);

    }

    /**
     * 替换数据并且存入到redis
     * @param apArticle
     * @param score
     * @param s
     */
    private void replaceDataToRedis(ApArticle apArticle, Integer score, String s) {
        String articleListStr = cacheService.get(s);
        if (StringUtils.isNotBlank(articleListStr)) {
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(articleListStr, HotArticleVo.class);

            boolean flag = true;

            //如果缓存中存在该文章，只更新分值
            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                if (hotArticleVo.getId().equals(apArticle.getId())) {
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }

            //如果缓存中不存在，查询缓存中分值最小的一条数据，进行分值的比较，如果当前文章的分值大于缓存中的数据，就替换
            if (flag) {
                if (hotArticleVoList.size() >= 30) {
                    hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                    HotArticleVo lastHot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    if (lastHot.getScore() < score) {
                        hotArticleVoList.remove(lastHot);
                        HotArticleVo hot = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle, hot);
                        hot.setScore(score);
                        hotArticleVoList.add(hot);
                    }


                } else {
                    HotArticleVo hot = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hot);
                    hot.setScore(score);
                    hotArticleVoList.add(hot);
                }
            }
            //缓存到redis
            hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
            cacheService.set(s, JSON.toJSONString(hotArticleVoList));

        }
    }

    /**
     * 更新文章行为数量
     * @param mess
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection()==null?0:apArticle.getCollection()+mess.getCollect());
        apArticle.setComment(apArticle.getComment()==null?0:apArticle.getComment()+mess.getComment());
        apArticle.setLikes(apArticle.getLikes()==null?0:apArticle.getLikes()+mess.getLike());
        apArticle.setViews(apArticle.getViews()==null?0:apArticle.getViews()+mess.getView());
        updateById(apArticle);
        return apArticle;

    }

    /**
     * 计算文章的具体分值
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if(apArticle.getLikes() != null){
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if(apArticle.getViews() != null){
            score += apArticle.getViews();
        }
        if(apArticle.getComment() != null){
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if(apArticle.getCollection() != null){
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return score;
    }

}
