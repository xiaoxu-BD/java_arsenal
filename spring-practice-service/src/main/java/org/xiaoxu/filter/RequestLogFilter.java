package org.xiaoxu.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * @className: RequestLogFilter
 * @description: 第一层 - Filter（Servlet容器层）
 */
@Slf4j
@Component
@Order(1)
public class RequestLogFilter implements Filter {

    private static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 生成traceId，塞入MDC
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);

        log.info("请求进入 | {} {}", request.getMethod(), request.getRequestURI());

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("请求结束 | 耗时={}ms", cost);
            // 必须清理，否则线程池复用时会串traceId
            MDC.clear();
        }
    }
}
