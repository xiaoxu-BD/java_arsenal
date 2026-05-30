package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.auth.LoginRequest;
import org.xiaoxu.auth.LoginUser;
import org.xiaoxu.common.utils.TokenProvider;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.mapper.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            return Result.error(401, "用户名或密码错误");
        } catch (UsernameNotFoundException e) {
            return Result.error(401, "用户不存在");
        } catch (Exception e) {
            return Result.error(500, "系统异常");
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
