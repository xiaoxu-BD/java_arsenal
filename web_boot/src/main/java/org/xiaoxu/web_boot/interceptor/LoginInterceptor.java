package org.xiaoxu.web_boot.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @className: LoginInterceptor
 * @author: xiaoxu
 * @date: 2025/9/1 9:25
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer-")) {
           log.info("[Interceptor] 未登录，拒绝访问！");
            response.setStatus(401); // 返回 401
            return false; //拦截请求 不进入到Controller
        }

        /// preHandle  在controller之前执行
        //放行 进入到Controller
        return true;
    }
}
