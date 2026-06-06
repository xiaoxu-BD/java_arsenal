package org.xiaoxu.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @className: HttpClientFactoryBean
 * @description: 演示FactoryBean，生产一个HttpClient对象
 *              创建过程复杂：配置超时、重定向策略等，适合用FactoryBean封装
 */
@Slf4j
@Component("myHttpClient")
public class HttpClientFactoryBean implements FactoryBean<HttpClient> {

    @Override
    public HttpClient getObject() throws Exception {
        log.info("FactoryBean.getObject() 被调用，正在创建HttpClient...");
        // 模拟复杂的构建过程
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        log.info("HttpClient 创建完成: {}", client);
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return HttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
