package org.xiaoxu.web_boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @className: RedisDEM
 * @author: xiaoxu
 * @date: 2025/11/3 18:12
 * @Version: 1.0
 * @description:
 */
@SpringBootTest
public class RedisDEM {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    // 测试Redis
    //get key   //incr key // del key  exist key //set key value

}
