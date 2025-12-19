package org.xiaoxu.web_boot;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.xiaoxu.web_boot.entity.User;
import org.xiaoxu.web_boot.service.RedisLockService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @className: RedisDEM
 * @author: xiaoxu
 * @date: 2025/11/3 18:12
 * @Version: 1.0
 * @description: 上班 就在我电脑上摸鱼,真的在摸鱼,然后周六周日 休息 想去学习什么的 根本静不下来 英雄联盟也不想玩,整天就是抖音
 */
@SpringBootTest
public class RedisDEM {


    private static final Logger log = LoggerFactory.getLogger(RedisDEM.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    // 测试Redis
    //get key   //incr key // del key  exist key //set key value

    @Autowired
    private RedissonClient redissonClient;


    @Test
    public void testBucket(){
        User user = new User();
        user.setId(1002L);
        user.setName("xxxxUUU");
        user.setAddress("SH");
        RBucket<User> bucket = redissonClient.getBucket("user:info:" + user.getId(), StringCodec.INSTANCE);
        bucket.set(user);
        bucket.expire(10, TimeUnit.MINUTES);
    }


    @Test
    public void testGet(){
        RBucket<User> bucket = redissonClient.getBucket("user:info:");
        User user = bucket.get();
        log.info("user: {}", user);
    }


    @Autowired
    private RedisLockService redisLockService;

    @Test
    public void testRedisDistributeLock() throws InterruptedException {
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            final String clientId = "Client-" + i;
            executor.submit(() -> {
                try {
                    String result = redisLockService.deductStock(clientId);
                    System.out.println(">>> " + result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }



}
