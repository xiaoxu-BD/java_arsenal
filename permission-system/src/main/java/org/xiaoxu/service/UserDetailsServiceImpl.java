package org.xiaoxu.service;

import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.xiaoxu.auth.LoginUser;
import org.xiaoxu.common.excepiton.user.AuthErrorCode;
import org.xiaoxu.common.excepiton.user.UserException;
import org.xiaoxu.mapper.*;
import org.xiaoxu.pojo.dto.SystemUserDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUserDTO user = userMapper.getOne(username);
        Assert.notNull(user,()-> new UserException(AuthErrorCode.USER_NAME_NOT_EXIST));

        // 查询用户角色ID  通过角色id 去查询 菜单表


        Set<Long> roleIds = userRoleMapper.getRoleIdsByUserId(user.getId());


        // roleIds 去 查询 菜单表

        List<Long> roleids = new ArrayList<>(roleIds);

//        Set<Long> menuIds = roleMenuMapper.getMenuIdsByRoleId(roleids);
//
//
//        Set<String> permissionsCode =    menuMapper.getPermissionCodeByMenuIds(menuIds);

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setPassword(user.getPassword());
        loginUser.setUsername(user.getUsername());
        loginUser.setPermissions(Collections.emptySet());
        return loginUser;
    }
}