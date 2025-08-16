package org.xiaoxu.web_boot.config.threadpool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @className: ThreadPoolTaskSchedule
 * @author: xiaoxu
 * @date: 2025/8/8 7:27
 * @Version: 1.0
 * @description:
 */
@Configuration
public class ThreadPoolTaskSchedule {


    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("my-schedule-task");
        ///等待终止时间
        scheduler.setAwaitTerminationSeconds(60); // 关闭时等待任务结束的秒数
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 等待任务结束
        return scheduler;
    }
}
