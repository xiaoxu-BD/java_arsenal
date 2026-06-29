package org.xiaoxu.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xiaoxu.common.utils.TokenProvider;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ((path.equals("/login") || path.equals("/auth/login") || path.contains("/api/auth/login"))
            && "POST".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            LoginUser loginUser = tokenProvider.getLoginUser(token);
            if (loginUser == null) {
                log.warn("Token 已失效: Redis 中不存在该 token");
                filterChain.doFilter(request, response);
                return;
            }
            // 每次有效请求续签 token
            tokenProvider.renewToken(token, loginUser.getUserId());

            List<SimpleGrantedAuthority> authorities = loginUser.getAuthorities().stream()
                    .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    loginUser.getUsername(), null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("userId", loginUser.getUserId());
            request.setAttribute("username", loginUser.getUsername());

            log.debug("Token 验证成功，用户: {}", loginUser.getUsername());
        } catch (Exception e) {
            log.error("Token 处理异常: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
