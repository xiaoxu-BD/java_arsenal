package org.xiaoxu.rocketmq.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 注解版学习工程入口。
 *
 * 与原生工程的根本区别：
 * 原生（rocketmq-native）：自己 new DefaultMQProducer/Consumer、自己 start/shutdown、自己管生命周期；
 * starter（本工程）：声明配置 + 注解，容器替你创建、启动、关闭客户端——
 * 你练过的每个 set 方法，都变成了注解的一个属性或 yml 里的一行配置（对照 README 映射表）。
 */
@SpringBootApplication
public class RocketMqSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(RocketMqSpringApplication.class, args);
    }
}
