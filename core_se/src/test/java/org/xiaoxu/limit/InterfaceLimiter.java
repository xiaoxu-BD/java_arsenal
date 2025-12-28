package org.xiaoxu.limit;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @className: InterfaceLimiter
 * @author: xiaoxu
 * @date: 2025/12/27 16:33
 * @Version: 1.0
 * @description:
 */
public class InterfaceLimiter {


    private static final RateLimiter rateLimiter = RateLimiter.create(5.0);


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);

        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // 尝试获取令牌，如果获取不到则等待
                    double waitTime = rateLimiter.acquire();
                    System.out.println("线程 " + Thread.currentThread().getName() + 
                        " 处理请求 " + index + 
                        "，等待时间：" + String.format("%.2f", waitTime) + "秒" +
                        "，实际时间：" + (System.currentTimeMillis() - startTime) + "ms");
                    // 模拟业务处理时间
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println("所有请求处理完成，总耗时：" + (endTime - startTime) + "ms");
    }
}
