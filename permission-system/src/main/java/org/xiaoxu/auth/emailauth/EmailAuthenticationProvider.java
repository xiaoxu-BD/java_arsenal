package org.xiaoxu.auth.emailauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.xiaoxu.common.excepiton.BizException;
import org.xiaoxu.mapper.MenuMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.service.SysUserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.xiaoxu.common.excepiton.user.AuthErrorCode.EMAIL_AUTH_CODE_ERROR;

/**
 * 邮箱验证码认证 Provider。
 * <p>
 * 执行流程：
 * 1. AuthenticationManager.authenticate() 收到 EmailAuthenticationToken
 * 2. 遍历 Provider 列表，调用 supports() 匹配到本 Provider
 * 3. 调用 authenticate()：Redis 校验验证码 → 查/建用户 → 构建已认证 Token
 */
@Slf4j
@Component
public class EmailAuthenticationProvider implements AuthenticationProvider {

    private static final String LOGIN_CODE_PREFIX = "login:code:";

    private final SysUserService sysUserService;
    private final MenuMapper menuMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public EmailAuthenticationProvider(SysUserService sysUserService, MenuMapper menuMapper,
                                       StringRedisTemplate stringRedisTemplate) {
        this.sysUserService = sysUserService;
        this.menuMapper = menuMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        EmailAuthenticationToken token = (EmailAuthenticationToken) authentication;
        String email = token.getEmail();
        String code = token.getCredentials().toString();

        // 1. 从 Redis 校验验证码
        String codeKey = LOGIN_CODE_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null) {
            throw new BizException(EMAIL_AUTH_CODE_ERROR, "验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            throw new BizException(EMAIL_AUTH_CODE_ERROR, "验证码错误");
        }
        stringRedisTemplate.delete(codeKey);

        // 2. 查找或创建用户（自动注册）
        SystemUsers user = sysUserService.findByEmail(email);
        if (user == null) {
            user = sysUserService.createByEmail(email);
            log.info("邮箱登录自动注册, email={}, userId={}", email, user.getId());
        }

        // 3. 加载权限
        Set<String> permissions = menuMapper.getPermissionCodeByUserId(user.getId());
        List<GrantedAuthority> authorities = permissions.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 4. 返回已认证的 Token（principal = username，后面 Controller 需要用）
        return new EmailAuthenticationToken(email, user, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
