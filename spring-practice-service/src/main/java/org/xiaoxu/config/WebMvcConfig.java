package org.xiaoxu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xiaoxu.interceptor.TimeCostInterceptor;

/**
 * @className: WebMvcConfig
 * @description: 注册拦截器，配置拦截路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TimeCostInterceptor timeCostInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(timeCostInterceptor)
                .addPathPatterns("/**")        // 拦截所有请求
                .excludePathPatterns("/static/**"); // 排除静态资源
    }
}
