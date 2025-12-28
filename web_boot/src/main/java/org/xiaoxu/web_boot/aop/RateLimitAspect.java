package org.xiaoxu.web_boot.aop;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {

        String key = pjp.getSignature().toLongString();

        RateLimiter limiter = limiterMap.computeIfAbsent(
            key, k -> RateLimiter.create(rateLimit.qps())
        );

        if (!limiter.tryAcquire()) {
            throw new RuntimeException("请求过多");
        }

        return pjp.proceed();
    }
}
