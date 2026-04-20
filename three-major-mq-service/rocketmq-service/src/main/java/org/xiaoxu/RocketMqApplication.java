package org.xiaoxu;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
public class RocketMqApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(RocketMqApplication.class, args);
    }
}
