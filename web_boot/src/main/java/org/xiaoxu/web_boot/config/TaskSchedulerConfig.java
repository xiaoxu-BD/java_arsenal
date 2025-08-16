package org.xiaoxu.web_boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @className: TaskSchedulerConfig
 * @author: xiaoxu
 * @date: 2025/8/8 7:30
 * @Version: 1.0
 * @description:
 */
@Configuration
public class TaskSchedulerConfig implements SchedulingConfigurer {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
