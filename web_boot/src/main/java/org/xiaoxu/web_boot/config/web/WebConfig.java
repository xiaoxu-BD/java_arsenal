package org.xiaoxu.web_boot.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xiaoxu.web_boot.interceptor.LogInterceptor;
import org.xiaoxu.web_boot.interceptor.LoginInterceptor;

/**
 * @className: WebConfig
 * @author: xiaoxu
 * @date: 2025/5/24 11:40
 * @Version: 1.0
 * @description:
 */
@RequiredArgsConstructor
@Component
public class WebConfig implements WebMvcConfigurer {
    private final LogInterceptor logInterceptor;
    private final LoginInterceptor loginInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor);
        // 注册登录拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/log/**")
                .excludePathPatterns("/admin/login","/api/order"); // 排除登录接口

    }
}
