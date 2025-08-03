package org.xiaoxu.config;

import jakarta.annotation.Resource;
import org.bouncycastle.util.encoders.UTF8;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.xiaoxu.utils.RedisStringUtil;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private RedisStringUtil redisUtil;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 定义白名单路径 (登录和注册接口)
        List<String> whitelist = List.of("/api/user/login", "/api/user/register");
        for (String pattern : whitelist) {
            if (antPathMatcher.match(pattern, path)) {
                return chain.filter(exchange); // 是白名单，直接放行
            }
        }

        // 2. 获取Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("bearer-")) {
            return unauthorizedResponse(exchange, "Token missing");
        }

        // 3. 验证Token
        String redisKey = "session:" + token;
        String userInfoJson =   redisUtil.get(redisKey);

        if (userInfoJson == null) {
            return unauthorizedResponse(exchange, "Token is valid");
        }

        // 4. (可选) 刷新Token有效期

//        redisTemplate.expire(redisKey, 30, java.util.concurrent.TimeUnit.MINUTES);
//
        // 5. 将用户信息传递给下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Info", userInfoJson)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1; // 保证最高优先级执行
    }

    // 辅助方法：返回未授权响应
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // 可以返回更详细的JSON错误信息
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}