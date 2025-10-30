package org.xiaoxu.web_boot.service;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 用Guava漏桶实现限流
 *
 * @author 湘王
 */
@Service
public class LeakyBucketService {
    // Guava限制每秒请求数量（漏桶的流速）
    private static final RateLimiter rateLimiter = RateLimiter.create(1);
    // 请求编号
    private volatile int count = 1;

    public boolean limit(HttpServletRequest request) throws InterruptedException {
        System.out.print("请求编号 = " + count++ + " ");
        // 休眠一秒，取决于机器性能
        Thread.sleep(1000);
        if (rateLimiter.tryAcquire()) {
            System.out.println(">>>>>>>> 请求成功 >>>>>>>>");
            return true;
        }
        System.out.println(">>>>>>>> 被限流了 >>>>>>>>");
        return false;
    }
}
