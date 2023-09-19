package com.iSchool.schedule.service;

import com.iSchool.model.schedule.dtos.Task;

/**
 * 对外访问接口
 */
public interface TaskService {

    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     */
    public void addTask(Task task);

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    public boolean cancelTask(long taskId);

    /**
     * 按照类型和优先级来拉取任务(消费任务)
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type,int priority);
}