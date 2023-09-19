package com.iSchool.schedule.fegin;

import com.iSchool.apis.schedule.IScheduleClient;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.schedule.dtos.Task;
import com.iSchool.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ScheduleClient implements IScheduleClient {
    @Autowired
    private TaskService taskService;

    /**
     * 添加任务
     * @param task
     * @return
     */
    @Override
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) {
        //long taskId = taskService.addTask(task);
        taskService.addTask(task);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    @GetMapping("/api/v1/task/cancel/{taskId}")
    public ResponseResult cancelTask(@PathVariable Long taskId) {
        boolean flag = taskService.cancelTask(taskId);
        //flag的判断交给调度方
        return ResponseResult.okResult(flag);
    }

    /**
     * 根据任务类型以及优先级拉取任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    @GetMapping("/api/v1/task/poll/{type}/{priority}")
    public ResponseResult poll(int type, int priority) {
        Task task = taskService.poll(type, priority);
        return ResponseResult.okResult(task);
    }

    @Override
    @GetMapping("/api/v1/task/test")
    public ResponseResult TestTask(){
        String mes="该请求没有问题";
        log.info("成功调用该请求");
        return ResponseResult.okResult(mes);
    }
}
