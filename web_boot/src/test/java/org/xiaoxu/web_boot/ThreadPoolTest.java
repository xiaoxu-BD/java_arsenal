package org.xiaoxu.web_boot;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
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


    @Autowired
    private RedissonClient redissonClient;

    @Test
    @SneakyThrows
    public void testCompletableFutureThread(){
        String lockName = "Lock:key:redisson";
        RLock lock  = redissonClient.getLock(lockName);

        // Create multiple competing threads
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // Create 5 threads competing for the same lock
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    log.info("线程 {} 尝试获取锁...", threadId);
                    boolean isLock = lock.tryLock(1000, 5000, TimeUnit.MILLISECONDS);
                    
                    if (isLock) {
                        log.info("获取锁成功线程:{},正在执行", Thread.currentThread().getName());
                        try {
                            // Hold the lock for 3 seconds
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            lock.unlock();
                            log.info("线程 {} 释放锁", threadId);
                        }
                    } else {
                        log.info("获取锁失败线程:{},未能获取到锁", Thread.currentThread().getName());
                    }
                } catch (Exception e) {
                    log.error("线程 {} 执行过程中发生异常", threadId, e);
                }
            });
            
            futures.add(future);
        }

        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        try {
            // Wait for up to 10 seconds for all threads to complete
            allFutures.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("测试超时，部分线程可能仍在运行");
        }

        log.info("测试完成");
    }
}