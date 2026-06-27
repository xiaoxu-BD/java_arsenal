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
import org.xiaoxu.auth.emailauth.EmailAuthenticationToken;
import org.xiaoxu.common.constants.CommonConstants;
import org.xiaoxu.common.constants.RedisKeyConstants;
import org.xiaoxu.common.utils.TokenProvider;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.request.EmailLoginRequest;
import org.xiaoxu.pojo.request.ResetPasswordByEmailRequest;
import org.xiaoxu.pojo.request.SendCodeRequest;
import org.xiaoxu.service.AuditLogService;
import org.xiaoxu.service.EmailService;
import org.xiaoxu.service.SysUserService;

import java.util.*;
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

    @Resource
    private SysUserService sysUserService;

    @Resource
    private AuditLogService auditLogService;

    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_MINUTES = 30;
    private static final long CODE_RATE_LIMIT_TTL = 240;
    private static final int MIN_PASSWORD_LENGTH = 6;

    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String failKey = RedisKeyConstants.LOGIN_FAIL_PREFIX + username;
        String ip = getClientIp(request);

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

            // 查询是否首次登录
            SystemUsers user = sysUserService.getUserById(loginUser.getUserId());
            boolean isFirstLogin = user != null && CommonConstants.FIRST_LOGIN_YES.equals(user.getFirstLogin());

            // 如果是首次登录，更新 firstLogin 为 0
            if (isFirstLogin) {
                user.setFirstLogin(CommonConstants.FIRST_LOGIN_NO);
                sysUserService.updateById(user);
            }

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("permissions", permissions);
            loginData.put("firstLogin", isFirstLogin);

            auditLogService.recordLoginLog(username, CommonConstants.LOGIN_TYPE_PASSWORD, ip, "", "", CommonConstants.LOG_STATUS_SUCCESS, "登录成功");
            return Result.success(loginData);

        } catch (BadCredentialsException e) {
            // 认证失败，计数 +1
            Long newCount = stringRedisTemplate.opsForValue().increment(failKey);
            // 第一次失败时设置过期时间
            if (newCount != null && newCount == 1) {
                stringRedisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
            }
            int remaining = MAX_FAIL_COUNT - (newCount != null ? newCount.intValue() : failCount + 1);
            String msg;
            if (remaining > 0) {
                msg = "用户名或密码错误，剩余" + remaining + "次尝试机会";
                auditLogService.recordLoginLog(username, CommonConstants.LOGIN_TYPE_PASSWORD, ip, "", "", CommonConstants.LOG_STATUS_FAILURE, msg);
                return Result.error(401, msg);
            } else {
                msg = "登录失败次数过多，账号已锁定" + LOCK_MINUTES + "分钟";
                auditLogService.recordLoginLog(username, CommonConstants.LOGIN_TYPE_PASSWORD, ip, "", "", CommonConstants.LOG_STATUS_FAILURE, msg);
                return Result.error(429, msg);
            }
        }
    }

    @GetMapping("/getUserInfo")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) authentication.getPrincipal();
        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 查询用户完整信息
        SystemUsers user = sysUserService.getUserById(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("userId", userId);
        data.put("permissions", permissions);
        if (user != null) {
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());
            data.put("email", user.getEmail());
            data.put("mobile", user.getMobile());
            data.put("sex", user.getSex());
        }
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader(CommonConstants.HEADER_AUTHORIZATION);
        if (token != null && token.startsWith(CommonConstants.BEARER_PREFIX)) {
            tokenProvider.removeToken(token.substring(7));
        }
        // 记录注销日志
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            auditLogService.recordLoginLog(auth.getName(), CommonConstants.LOGIN_TYPE_LOGOUT, getClientIp(request), "", "", CommonConstants.LOG_STATUS_SUCCESS, "用户注销");
        }
        return Result.success();
    }

    // ==================== 邮箱验证码登录 ====================

    @Autowired(required = false)
    private EmailService emailService;

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendCode")
    public Result<?> sendCode(@RequestBody @Valid SendCodeRequest request) {
        String email = request.getEmail();
        if (emailService == null) {
            return Result.error(500, "邮件服务未配置");
        }

        // 60 秒防刷
        String codeKey = RedisKeyConstants.LOGIN_CODE_PREFIX + email;
        Long ttl = stringRedisTemplate.getExpire(codeKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > CODE_RATE_LIMIT_TTL) {
            return Result.error(429, "验证码已发送，请" + ttl + "秒后重试");
        }

        // 生成 6 位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存 Redis，5 分钟过期
        stringRedisTemplate.opsForValue().set(codeKey, code, RedisKeyConstants.CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发邮件
        String html = emailService.buildEmailHtml(
                "您的登录验证码",
                "您正在登录 BPMN 权限管理系统，验证码如下：",
                "<b>验证码：</b>" + code + "<br><b>有效期：</b>5 分钟"
        );
        emailService.sendHtmlEmail(email, "【BPMN系统】登录验证码", html);

        log.info("验证码已发送, email={}", email);
        return Result.success("验证码已发送");
    }

    /**
     * 邮箱验证码登录（不存在则自动注册）
     */
    @PostMapping("/emailLogin")
    public Result<?> emailLogin(@RequestBody EmailLoginRequest emailLoginRequest, HttpServletRequest request) {
        String email = emailLoginRequest.getEmail();
        String code = emailLoginRequest.getCode();
        String ip = getClientIp(request);

        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            return Result.error(400, "邮箱和验证码不能为空");
        }

        try {
            // 构造未认证 Token → 交给 AuthenticationManager → 路由到 EmailAuthenticationProvider
            EmailAuthenticationToken authToken = new EmailAuthenticationToken(email, code);
            Authentication result = authenticationManager.authenticate(authToken);

            // 从已认证 Token 中取出用户对象和权限
            EmailAuthenticationToken emailResult = (EmailAuthenticationToken) result;
            SystemUsers user = emailResult.getSystemUser();
            List<String> permissions = result.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .toList();

            // 生成 JWT token（复用 LoginUser 作为载体）
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getId());
            loginUser.setUsername(user.getUsername());
            loginUser.setPassword(user.getPassword());
            loginUser.setPermissions(new java.util.HashSet<>(permissions));
            String token = tokenProvider.createToken(loginUser);

            boolean isFirstLogin = CommonConstants.FIRST_LOGIN_YES.equals(user.getFirstLogin());

            // 如果是首次登录，更新 firstLogin 为 0
            if (isFirstLogin) {
                user.setFirstLogin(CommonConstants.FIRST_LOGIN_NO);
                sysUserService.updateById(user);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("permissions", permissions);
            data.put("firstLogin", isFirstLogin);

            // 记录邮箱登录成功日志
            auditLogService.recordLoginLog(user.getUsername(), CommonConstants.LOGIN_TYPE_EMAIL, ip, "", "", CommonConstants.LOG_STATUS_SUCCESS, "邮箱登录成功");
            return Result.success(data);

        } catch (Exception e) {
            // 记录邮箱登录失败日志
            auditLogService.recordLoginLog(email, CommonConstants.LOGIN_TYPE_EMAIL, ip, "", "", CommonConstants.LOG_STATUS_FAILURE, "邮箱登录失败: " + e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 通过邮箱验证码设置密码（用于邮箱注册用户首次设密码）
     */
    @PostMapping("/resetPasswordByEmail")
    public Result<?> resetPasswordByEmail(@RequestBody @Valid ResetPasswordByEmailRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            return Result.error(400, "密码长度不能少于 " + MIN_PASSWORD_LENGTH + " 位");
        }

        // 校验验证码
        String codeKey = RedisKeyConstants.LOGIN_CODE_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null) {
            return Result.error(400, "验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            return Result.error(400, "验证码错误");
        }
        stringRedisTemplate.delete(codeKey);

        // 查找用户
        SystemUsers user = sysUserService.findByEmail(email);
        if (user == null) {
            return Result.error(404, "该邮箱未注册");
        }

        // 设置新密码
        sysUserService.resetPassword(user.getId(), newPassword);
        log.info("邮箱验证码重置密码成功, email={}", email);
        return Result.success("密码设置成功，请使用新密码登录");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(CommonConstants.HEADER_X_FORWARDED_FOR);
        if (ip == null || ip.isEmpty() || CommonConstants.UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader(CommonConstants.HEADER_X_REAL_IP);
        }
        if (ip == null || ip.isEmpty() || CommonConstants.UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
