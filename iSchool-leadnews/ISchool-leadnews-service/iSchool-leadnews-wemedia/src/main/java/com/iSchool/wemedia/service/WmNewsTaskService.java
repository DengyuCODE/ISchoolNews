package com.iSchool.wemedia.service;

import java.util.Date;

public interface WmNewsTaskService {
    /**
     * 将提交审核文章加入到消费队列
     * @param newsId
     * @param publishTime
     */
    public void addNewsToTask(Integer newsId, Date publishTime);

    /**
     * 消费延迟队列
     */
    public void scanNewsByTask();
}
