package com.iSchool.behavior.service.Impl;

import com.alibaba.fastjson.JSON;
import com.iSchool.behavior.service.ApReadBehaviorService;
import com.iSchool.common.constants.BehaviorConstants;
import com.iSchool.common.constants.HotArticleConstants;
import com.iSchool.common.redis.CacheService;
import com.iSchool.model.behavior.dtos.ReadBehaviorDto;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.mess.UpdateArticleMess;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        //1.检查参数
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.是否登录
        ApUser user = ApThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //更新阅读次数
        //获取redis中的数据
        String readBehaviorJson = (String) cacheService.hGet(BehaviorConstants.READ_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
        //数据不为空使阅读次数加1
        if (StringUtils.isNotBlank(readBehaviorJson)) {
            ReadBehaviorDto readBehaviorDto = JSON.parseObject(readBehaviorJson, ReadBehaviorDto.class);
            dto.setCount((short) (readBehaviorDto.getCount() + dto.getCount()));
        }
        // 保存当前key
        log.info("保存当前key:{} {} {}", dto.getArticleId(), user.getId(), dto);
        cacheService.hPut(BehaviorConstants.READ_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString(), JSON.toJSONString(dto));

        //发送消息，数据聚合
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(dto.getArticleId());
        mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);
        mess.setAdd(1);
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC,JSON.toJSONString(mess));


        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }
}
