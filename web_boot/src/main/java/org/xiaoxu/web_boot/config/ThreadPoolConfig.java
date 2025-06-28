package org.xiaoxu.web_boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Primary
    @Bean
    public ThreadPoolExecutor highThread() {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("OrderHighThread-");
        return new ThreadPoolExecutor(100, 100, 0,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(100000), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public ThreadPoolExecutor lowThread() {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("OrderLowThread-");
        return new ThreadPoolExecutor(25, 25, 0,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(100000), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


    }