package com.iSchool.wemedia;

import com.iSchool.apis.schedule.IScheduleClient;
import com.iSchool.model.common.dtos.ResponseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class FeginTest {
    @Autowired
    private IScheduleClient scheduleClient;

    @Test
    public void testScanText() throws Exception {
        ResponseResult responseResult = scheduleClient.TestTask();
        System.out.println(responseResult);
    }
}
