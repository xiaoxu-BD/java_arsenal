package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.auth.LoginRequest;
import org.xiaoxu.auth.LoginUser;
import org.xiaoxu.common.utils.TokenProvider;
import org.xiaoxu.common.utils.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @className: AuthController
 * @author: xiaoxu
 * @date: 2025/11/17 14:53
 * @Version: 1.0
 * @description:
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/")
public class AuthController {


    @GetMapping("/hello")
    public String sayHello() {
        return "hello world!";
    }


    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider tokenProvider;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_MINUTES = 30;

    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String failKey = LOGIN_FAIL_PREFIX + username;

        // 1. 检查是否被锁定
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        if (failCount >= MAX_FAIL_COUNT) {
            Long ttl = stringRedisTemplate.getExpire(failKey, TimeUnit.MINUTES);
            return Result.error(429, "登录失败次数过多，请" + ttl + "分钟后重试");
        }

        // 2. 尝试认证
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            // 认证成功，清除失败计数
            stringRedisTemplate.delete(failKey);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            String token = tokenProvider.createToken(loginUser);

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("permissions", permissions);
            return Result.success(loginData);

        } catch (BadCredentialsException e) {
            // 认证失败，计数 +1
            Long newCount = stringRedisTemplate.opsForValue().increment(failKey);
            // 第一次失败时设置过期时间
            if (newCount != null && newCount == 1) {
                stringRedisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
            }
            int remaining = MAX_FAIL_COUNT - (newCount != null ? newCount.intValue() : failCount + 1);
            if (remaining > 0) {
                return Result.error(401, "用户名或密码错误，剩余" + remaining + "次尝试机会");
            } else {
                return Result.error(429, "登录失败次数过多，账号已锁定" + LOCK_MINUTES + "分钟");
            }
        }
    }


    @GetMapping("/getUserInfo")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Object userId = request.getAttribute("userId");
        String username = (String) authentication.getPrincipal();
        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("userId", userId);
        data.put("permissions", permissions);
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            tokenProvider.removeToken(token.substring(7));
        }
        return Result.success();
    }

}
