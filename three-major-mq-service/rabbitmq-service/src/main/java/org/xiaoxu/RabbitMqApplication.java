package org.xiaoxu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class RabbitMqApplication {
    public static void main( String[] args ) {
        ConfigurableApplicationContext ctx = SpringApplication.run(RabbitMqApplication.class, args);
    }
}
