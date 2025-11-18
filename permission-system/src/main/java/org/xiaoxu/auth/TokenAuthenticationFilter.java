package org.xiaoxu.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xiaoxu.common.utils.JwtTokenProvider;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @className: TokenAuthenticationFilter
 * @author: xiaoxu
 * @date: 2025/11/17 14:02
 * @Version: 1.0
 * @description: Token 认证过滤器 - 验证 JWT token 并设置认证信息
 */
@Component
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired(required = false)
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 没有 Token，放行（后续 SecurityConfig 会拦截未认证的请求）
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 提取 token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);

        try {
            // 3. 先检查 Redis 中是否存在该 token（优先级更高：支持应用重启后 token 继续有效）
            String cacheKey = "login:token:" + token;
            Object cachedUsername = redisTemplate.opsForValue().get(cacheKey);
            if (cachedUsername == null) {
                log.warn("Token 已失效: Redis 中不存在该 token，可能已登出");
                filterChain.doFilter(request, response);
                return;
            }

            // 4. 尝试从 token 中提取用户信息（验证签名）
            String username;
            List<String> permissions;
            
            try {
                // 尝试解析 token（会验证签名和过期时间）
                username = jwtTokenProvider.getUsername(token);
                permissions = jwtTokenProvider.getPermissions(token);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // JWT 过期，但 Redis 中有 token，说明应用重启了
                // 从过期的 token 中提取信息（ExpiredJwtException 的 getClaims() 仍然可以访问）
                log.debug("JWT 已过期，但 Redis 中存在该 token，允许通过（应用重启场景）");
                username = e.getClaims().getSubject();
                permissions = (List<String>) e.getClaims().get("permissions");
                if (permissions == null) {
                    permissions = List.of(); // 如果权限为空，使用空列表
                }
            } catch (Exception e) {
                // 签名验证失败或其他错误，拒绝
                log.warn("Token 签名验证失败: {}", e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            // 6. 构造 Authentication 对象
            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username,  // principal（可以是 username 或 UserDetails）
                            null,       // credentials（密码不需要）
                            authorities // 权限列表
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Token 验证成功，用户: {}", username);

        } catch (Exception e) {
            log.error("Token 处理异常: {}", e.getMessage(), e);
            // 异常时清除认证信息，确保安全
            SecurityContextHolder.clearContext();
        }

        // 8. 继续过滤器链
        filterChain.doFilter(request, response);
    }
}