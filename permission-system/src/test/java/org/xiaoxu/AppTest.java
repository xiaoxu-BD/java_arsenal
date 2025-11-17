package org.xiaoxu;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Unit test for simple App.
 */
@SpringBootTest
public class AppTest{

    @Autowired
    private RedisTemplate redisTemplate;



    @Test
    public void test() {
        redisTemplate.opsForValue().set("name", "xiaoxu");
        System.out.println(redisTemplate.opsForValue().get("name"));
    }

}
