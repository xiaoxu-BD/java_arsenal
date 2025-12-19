package org.xiaoxu.web_boot.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @className: RedisLockService
 * @author: xiaoxu
 * @date: 2025/12/17 21:30
 * @Version: 1.0
 * @description:
 */
@Service
public class RedisLockService {

    @Autowired
    private RedissonClient redissonClient;

    private final String LOCK_KEY = "lock:stock:product_001";
    private int stock = 5; // 模拟库存

    public String deductStock(String clientId) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_KEY);

        boolean isLocked = false;
        try {
            // 尝试获取锁，最多等 5 秒，上锁后 10 秒自动释放（防止死锁）
            isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                return clientId + " ❌ 获取锁失败，被限流";
            }

            System.out.println(clientId + " ✅ 获得锁，开始扣减库存...");

            if (stock <= 0) {
                return clientId + " ⚠️ 库存不足";
            }

            Thread.sleep(2000); // 模拟业务耗时（比如调用支付）

            stock--;
            System.out.println(clientId + " ➖ 扣减成功，剩余库存：" + stock);
            return clientId + " ✅ 扣减成功，剩余库存：" + stock;

        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println(clientId + " 🔓 释放锁");
            }
        }
    }
}
