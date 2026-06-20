package org.xiaoxu.auth.emailauth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.xiaoxu.pojo.SystemUsers;

import java.util.Collection;

/**
 * 邮箱验证码认证 Token。
 * <p>
 * email → getName()
 * code  → getCredentials()
 * 认证成功后：principal = SystemUsers 对象
 */
public class EmailAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String email;

    /**
     * 未认证：传入 email + code
     */
    public EmailAuthenticationToken(String email, String code) {
        super(email, code);
        this.email = email;
    }

    /**
     * 已认证：传入 email + user 对象 + 权限
     */
    public EmailAuthenticationToken(String email, SystemUsers user, Collection<? extends GrantedAuthority> authorities) {
        super(user, null, authorities);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    /**
     * 获取已认证的用户对象
     */
    public SystemUsers getSystemUser() {
        return (SystemUsers) getPrincipal();
    }
}
