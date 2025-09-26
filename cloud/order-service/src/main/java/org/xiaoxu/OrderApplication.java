package org.xiaoxu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Hello world!
 *
 */
/**
 * 启用服务发现客户端功能，允许该服务被注册到服务注册中心（如Eureka、Consul等）
 * 以便其他服务可以通过服务名来发现和调用此服务
 */
@EnableDiscoveryClient
@SpringBootApplication
public class OrderApplication implements CommandLineRunner {
    public static void main( String[] args )
    {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Override
    public void run(String... args) throws Exception {
        if (redisConnectionFactory instanceof LettuceConnectionFactory) {
            LettuceConnectionFactory factory = (LettuceConnectionFactory) redisConnectionFactory;

            System.out.println("🔴 Redis Host: " + factory.getHostName());
            System.out.println("🔴 Redis Port: " + factory.getPort());
            System.out.println("🔴 Redis Password: " + (factory.getPassword() != null ? "[SET]" : "null"));
            System.out.println("🔴 Redis Database: " + factory.getDatabase());
        } else {
            System.out.println("当前 Redis 连接工厂不是 Lettuce，是: " + redisConnectionFactory.getClass());
        }
    }
}

