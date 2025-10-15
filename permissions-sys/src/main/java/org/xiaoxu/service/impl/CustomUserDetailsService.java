package org.xiaoxu.service.impl;

import jakarta.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.res.UserDTO;
import org.xiaoxu.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * @className: CustomUserDetailsService
 * @author: xiaoxu
 * @date: 2025/10/6 16:33
 * @Version: 1.0
 * @description:
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Resource
    private UserService userService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDTO user = userService.getByUsername(username);
        Assert.notNull(user, "用户不存在");
       List<GrantedAuthority> authorities  = new ArrayList<>();

        // 根据权限 code 填充 authorities
        user.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getCode())));

        // 也可以把 role code 当 ROLE_ 前缀放入 authorities
        user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getCode())));

    //sso

        //flowable 用户 / 部门的 权限
        return new User(user.getUsername(), user.getEnabled(), true,true, true, true, authorities);
    }
}
