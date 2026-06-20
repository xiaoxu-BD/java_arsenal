package org.xiaoxu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.xiaoxu.auth.LoginUser;
import org.xiaoxu.common.excepiton.user.AuthErrorCode;
import org.xiaoxu.mapper.*;
import org.xiaoxu.pojo.dto.SystemUserDTO;

import java.util.Set;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MenuMapper menuMapper;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUserDTO user = userMapper.getOne(username);
        // 抛 Spring Security 标准的 UsernameNotFoundException，
        // 配合 SecurityConfig 中 setHideUserNotFoundExceptions(false)，
        // 让 GlobalExceptionHandler 的 UsernameNotFoundException handler 能直接接到。
        if (user == null) {
            throw new UsernameNotFoundException(
                    AuthErrorCode.USER_NAME_NOT_EXIST.getMessage() + ": " + username);
        }

        // 查询用户角色ID  通过角色id 去查询 菜单表
/*
         直接一部到位:
         SELECT DISTINCT sm.permission_code
         FROM sys_menu sm
         JOIN system_role_menu srm ON sm.id = srm.menu_id
         JOIN system_user_role ur ON srm.role_id = ur.role_id
         WHERE ur.user_id = #{userId}
*/

        Set<String> permissionsCode =  menuMapper.getPermissionCodeByUserId(user.getId());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setPassword(user.getPassword());
        loginUser.setUsername(user.getUsername());
        loginUser.setPermissions(permissionsCode);
        return loginUser;
    }
}