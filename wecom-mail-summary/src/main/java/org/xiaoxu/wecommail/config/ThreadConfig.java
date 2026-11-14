package org.xiaoxu.wecommail.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadConfig {



    @Bean
    public ExecutorService taskExecutor() {
        return new ThreadPoolExecutor(
                10, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactoryBuilder().setNameFormat("async-task-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
