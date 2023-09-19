package com.iSchool.apis.schedule;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-schedule")
public interface IScheduleClient {

    /**
     * 添加任务
     * @param task
     */
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task);

    /**
     * 取消任务
     * @param taskId
     */
    @GetMapping("/api/v1/task/cancel/{taskId}")
    public ResponseResult cancelTask(@PathVariable Long taskId);


    /**
     * 根据任务类型以及优先级拉取任务
     * @param type
     * @param priority
     */
    @GetMapping("/api/v1/task/poll/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type,@PathVariable("priority") int priority);

    @GetMapping("/api/v1/task/test")
    public ResponseResult TestTask();
}
