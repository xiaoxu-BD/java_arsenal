package org.xiaoxu.auth;

import cn.hutool.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.xiaoxu.common.utils.JwtTokenProvider;

import java.io.IOException;
import java.util.Collections;

/**
 * @className: TokenAuthenticationFilter
 * @author: xiaoxu
 * @date: 2025/11/17 14:02
 * @Version: 1.0
 * @description:
 */
@Service
public class TokenAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头获取 Token
        String token = request.getHeader("Authorization"); // 假设 Token 放在 Authorization 头

        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 没有 Token，放行 (后续 SecurityConfig 会拦截)
            return;
        }

        token = token.substring(7); // 去掉 "Bearer "

        // 2. 解析 Token，获取用户标识 (例如 userId)
        String userId = null;
        

        filterChain.doFilter(request, response);
    }


    //从token中拿 用户登录信息
}