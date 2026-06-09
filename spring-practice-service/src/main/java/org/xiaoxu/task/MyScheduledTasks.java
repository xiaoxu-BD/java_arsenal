package org.xiaoxu.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @className: MyScheduledTasks
 * @author: xiaoxu
 * @date: 2025/11/20 10:54
 * @Version: 1.0
 * @description:
 */
@Component
public class MyScheduledTasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyScheduledTasks.class);

    // 1. 固定频率（每5秒执行一次，不管上一次是否执行完）
//    @Scheduled(fixedRate = 5000)
    public void task1() {
        LOGGER.info("fixedRate 执行了...");
        // sleep(10000); // 如果上一个还没执行完，下一个会立刻启动（可能多线程并发）
    }

    // 2. 固定延迟（上一个执行完后，延迟5秒再执行下一个）
//    @Scheduled(fixedDelay = 5000)
    public void task2() {
        LOGGER.info("fixedDelay 执行了...");
    }

    // 3. 持久化签到记录已迁移到 XXL-Job handler，见 SignJobHandler.persistSignIns
}
