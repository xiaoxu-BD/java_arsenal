package org.xiaoxu.web_boot;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @className: ThreadPoolTest
 * @author: xiaoxu
 * @date: 2025/9/4 14:18
 * @Version: 1.0
 * @description:
 */
@SpringBootTest
public class ThreadPoolTest {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolTest.class);
    private static final int taskCount = 20;
    @Autowired
    @Qualifier("commonThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Test
    public void testTaskExecutor() throws InterruptedException {
//        CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            CompletableFuture.runAsync(()->{
                log.info("线程:{},正在执行", Thread.currentThread().getName() + "任务id:" + taskId);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
//                    countDownLatch.countDown();
                }
            }, taskExecutor);
        }
//        countDownLatch.await();
    }



}
