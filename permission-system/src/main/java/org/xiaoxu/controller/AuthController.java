package org.xiaoxu.controller;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.auth.LoginRequest;
import org.xiaoxu.common.excepiton.user.AuthErrorCode;
import org.xiaoxu.common.excepiton.user.UserException;
import org.xiaoxu.common.utils.JwtTokenProvider;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.mapper.*;
import org.xiaoxu.pojo.dto.SystemUserDTO;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @className: AuthController
 * @author: xiaoxu
 * @date: 2025/11/17 14:53
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/")
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


    @PostMapping("login")
    public Result<?> login(@RequestBody LoginRequest loginRequest){

        Authentication authentication = authenticationManager.authenticate(
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
        
//        // 5. 返回菜单
//        SystemUserDTO user = userMapper.getOne(userDetails.getUsername());
//        Assert.notNull(user,()-> new UserException(AuthErrorCode.USER_NAME_NOT_EXIST));
        // 查询用户角色ID  通过角色id 去查询 菜单表
        // Set<Long> roleIds = userRoleMapper.getRoleIdsByUserId(user.getId());
        // // roleIds 去 查询 菜单表
        // List<Long> roleids = new ArrayList<>(roleIds);
        // Set<Long> menuIds = roleMenuMapper.getMenuIdsByRoleId(roleids);
        // Set<String> permissionsCode =    menuMapper.getPermissionCodeByMenuIds(menuIds);



        return Result.success(token);

    }



}
