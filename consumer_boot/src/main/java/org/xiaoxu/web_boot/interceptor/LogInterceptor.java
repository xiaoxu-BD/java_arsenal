package org.xiaoxu.web_boot.interceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取 traceId，如果没有则生成新的
        String tid = request.getHeader(TRACE_ID);
        if (StringUtils.isEmpty(tid)) {
            tid = UUID.randomUUID().toString().replaceAll("-", "");
        }

        // 存入 MDC
        MDC.put(TRACE_ID, tid);

        // 设置到响应头，便于下游服务获取
        response.setHeader(TRACE_ID, tid);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 MDC，避免线程复用时泄漏
        MDC.remove(TRACE_ID);
    }
}