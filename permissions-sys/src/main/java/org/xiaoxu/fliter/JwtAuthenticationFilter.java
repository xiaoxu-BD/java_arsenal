package org.xiaoxu.fliter;

import cn.hutool.jwt.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xiaoxu.service.impl.CustomUserDetailsService;
import org.xiaoxu.utils.JwtUtil;

import java.io.IOException;

/**
 * @className: JwtAuthenticationFilter
 * @author: xiaoxu
 * @date: 2025/10/6 17:10
 * @Version: 1.0
 * @description:
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try
            {
             Claims claims = (Claims) JwtUtil.parseAndVerifyToken(token, JwtUtil.secret);
                String username = (String)claims.getClaim("username");

                //校验:
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);


                // 创建一个认证对象，包含用户信息和权限信息
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null,  // 凭证信息，JWT场景下通常为null
                        userDetails.getAuthorities()  // 用户的角色权限列表
                );
                
                // 将认证对象存入Spring Security的上下文，表示该用户已通过认证
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            }catch (Exception e){
                // token 无效 -> 忽略，不放行，后续会被认证规则拦截
            }
        }


        filterChain.doFilter(request, response);
    }
}
