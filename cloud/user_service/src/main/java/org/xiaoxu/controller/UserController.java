package org.xiaoxu.controller;

import cn.hutool.json.JSONUtil;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.entity.User;
import org.xiaoxu.service.UserService;
import org.xiaoxu.utils.RedisStringUtil;
import org.xiaoxu.utils.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @className: UserController
 * @author: xiaoxu
 * @date: 2025/6/4 22:39
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api/user")
public class UserController {


    @GetMapping("/hi")
    public String hi(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        // 获取服务器接收此请求的端口号
        int serverPort = request.getServerPort();

        System.out.println("PATH: " + requestUri);
        System.out.println("PORT: " + serverPort); // 在这里打印端口号

        return "Spring Cloud Gateway!";
    }


    @Autowired
    private UserService userService;

    @Resource
    private RedisStringUtil redisUtil;

    @PostMapping("/login")
    public Result login(@RequestBody User loginUser) {
        User dbUser = userService.login(loginUser.getName(), loginUser.getPassword());
        if (dbUser != null) {
            // 1. 生成唯一Token
            String token = "bearer-" + UUID.randomUUID().toString().replace("-", "");
            // 2. 将用户信息存入Redis，设置有效期（例如30分钟）
            // 隐藏敏感信息
            dbUser.setPassword(null);
            String userJson = JSONUtil.toJsonStr(dbUser);
            redisUtil.set("session:" + token, userJson, 30, TimeUnit.MINUTES);
            // 3. 返回Token给前端
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", dbUser);
            return Result.success(data);
        }
        return Result.error("用户名或密码错误");
    }
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        // 从Redis中删除Token
        redisUtil.delete("session:" + token);
        return Result.success("注销成功");
    }

    @PostMapping("/register")
    public Result register(@RequestBody User registerUser) {
        // 调用UserService注册用户
        User registeredUser = userService.register(registerUser);
        if (registeredUser != null) {
            return Result.success(registeredUser);
        }
        return Result.error("注册失败");
    }

    // 获取当前登录用户信息接口
    @GetMapping("/me")
    public Result getMyInfo(@RequestHeader("X-User-Info") String userInfoJson) {
        // 网关已经从Redis中获取了用户信息，并放在请求头里
        User currentUser = JSONUtil.toBean(userInfoJson, User.class);
        return Result.success(currentUser);
    }

}
