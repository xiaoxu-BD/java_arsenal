package org.xiaoxu.controller;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.auth.LoginRequest;
import org.xiaoxu.common.utils.JwtTokenProvider;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.mapper.*;

import java.awt.*;
import java.util.List;
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
    private JwtTokenProvider jwtTokenProvider;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;


    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest loginRequest){

        Authentication authentication = null;
        try {
            // 调用：UsernamePasswordAuthenticationFilter
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            //将认证信息存储在SecurityContextHolder中
            SecurityContextHolder.getContext().setAuthentication(authentication);


            String username = loginRequest.getUsername();
            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            String token = jwtTokenProvider.createToken(username, permissions);

            // 4. 缓存用户信息（Redis）
            String cacheKey = "login:token:" + token;
            // Instead of storing the authentication object directly, store user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            redisTemplate.opsForValue().set(cacheKey, userDetails.getUsername(), 1, TimeUnit.HOURS);
            return Result.success(token);
        } catch (BadCredentialsException e) {
            return Result.error(401, "用户名或密码错误");
        } catch (UsernameNotFoundException e) {
            return Result.error(401, "用户不存在");
        } catch (Exception e) {
            return Result.error(500, "系统异常");
        }

    }



}
