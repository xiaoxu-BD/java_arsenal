package org.xiaoxu.web_boot.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @className: RedissonConfig
 * @author: xiaoxu
 * @date: 2025/9/3 21:47
 * @Version: 1.0
 * @description: Redisson配置类型,主要配置单机和密码
 */
@Configuration
public class RedissonConfig{


    @Bean
    public RedissonClient redissonInLocal(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:32768");

        config.setThreads(16);
        config.setNettyThreads(32);

        return Redisson.create(config);
    }
//    @Bean
//    public RedissonClient redisson(){
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://192.168.200.128:6379")
//                .setPassword("NFTurbo666")
//               .setDatabase(9);
//
//         config.setThreads(16);
//         config.setNettyThreads(32);
//
//         return Redisson.create(config);
//    }


}
