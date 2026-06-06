package org.xiaoxu.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @className: TimeCostInterceptor
 * @description: 第二层 - Interceptor（Spring MVC层）
 */
@Slf4j
@Component
public class TimeCostInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("interceptorStart", System.currentTimeMillis());

        // handler 是 HandlerMethod，能拿到Controller方法的详细信息
        if (handler instanceof HandlerMethod handlerMethod) {
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            log.info("【2-Interceptor】前置 | 目标方法: {}.{}", className, methodName);
        }
        return true; // 放行
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, org.springframework.web.servlet.ModelAndView modelAndView) {
        log.info("【2-Interceptor】中置 | Controller已执行，视图渲染前");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long start = (long) request.getAttribute("interceptorStart");
        long cost = System.currentTimeMillis() - start;
        log.info("【2-Interceptor】后置 | 视图渲染完成 | 耗时={}ms", cost);
    }
}
