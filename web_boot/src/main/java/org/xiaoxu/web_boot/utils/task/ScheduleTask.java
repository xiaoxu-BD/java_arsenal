package org.xiaoxu.web_boot.utils.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @className: ScheduleTask
 * @author: xiaoxu
 * @date: 2025/8/5 7:47
 * @Version: 1.0
 * @description:
 */
@Component
@Slf4j
public class ScheduleTask {

    /**
     * fixedRate 表示每隔多久执行一次，单位毫秒
     * fixedDelay 	每次执行完后隔  秒再次执行
     * initialDelay 	初始延迟  秒后执行
     * cron            表达式，每隔  秒执行一次
     * 默认单线程，多任务串行，如果需要并行，需要配置线程池
     */
//
//    @Scheduled(fixedRate = 5000)
//    public void scheduleTask() throws InterruptedException {
//        System.out.println(Thread.currentThread().getName() + " -> task1 start");
//        Thread.sleep(3000);
//        System.out.println(Thread.currentThread().getName() + " -> task1 end");
//    }
//
//    @Scheduled(fixedRate = 2000)
//    public void task2() {
//        System.out.println(Thread.currentThread().getName() + " -> task2 run");
//    }
}
