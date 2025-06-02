package org.xiaoxu.web_boot.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xiaoxu.web_boot.interceptor.LogInterceptor;

/**
 * @className: WebConfig
 * @author: xiaoxu
 * @date: 2025/5/24 11:40
 * @Version: 1.0
 * @description:
 */
//@RequiredArgsConstructor
@Component
public class WebConfig implements WebMvcConfigurer {
    private final LogInterceptor logInterceptor;

    public WebConfig(LogInterceptor logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor);
    }
}
