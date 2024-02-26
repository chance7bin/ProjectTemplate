package com.example.projecttemplate.controller;

import com.example.projecttemplate.entity.dto.ApiResponse;
import com.example.projecttemplate.manager.ThreadPoolManager;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.TimerTask;

/**
 * 测试接口
 *
 * @author 7bin
 * @date 2024/02/26
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试接口
     *
     * @return {@link ApiResponse}
     */
    @ApiOperation(value = "测试接口")
    @GetMapping("/test")
    public ApiResponse test() {

        // 执行异步任务
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("异步任务执行...");
            }
        };
        ThreadPoolManager.instance().execute(task);

        return ApiResponse.success("test success");
    }

}
