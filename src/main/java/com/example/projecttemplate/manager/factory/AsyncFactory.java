package com.example.projecttemplate.manager.factory;

import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 * 
 * @author 7bin
 */
@Slf4j
public class AsyncFactory {

    public static TimerTask demoTask() {
        return new TimerTask() {
            @Override
            public void run() {
                log.info("执行成功");
            }
        };
    }

}
