package org.xiaoxu.inceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @className: TokenInceptor
 * @author: xiaoxu
 * @date: 2025/10/8 17:50
 * @Version: 1.0
 * @description:
 */
@Component
public class TokenInceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("token", UUID.randomUUID().toString());
    }
}
