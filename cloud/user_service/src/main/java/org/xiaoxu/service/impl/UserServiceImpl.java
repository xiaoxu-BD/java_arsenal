package org.xiaoxu.service.impl;

import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Service;
import org.xiaoxu.entity.User;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.service.UserService;

import java.util.UUID;

/**
 * @className: UserServiceImpl
 * @author: xiaoxu
 * @date: 2025/8/3 13:39
 * @Version: 1.0
 * @description:
 */
@Service
public class UserServiceImpl implements UserService {
    //推荐使用构造器注入
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User login(String username, String password) {
        // 1. 根据用户名从数据库查找用户
        User dbUser = userMapper.getUserByName(username);
        if (dbUser == null) {
            return null;
        }
        if (password.equals(dbUser.getPassword())) {
            // 密码匹配，登录成功
            return dbUser;
        } else {
            // 密码不匹配，登录失败
            return null;
        }
    }

    @Override
    public User register(User registerUser) {
        // 1. 检查用户名是否已存在
        User existingUser = userMapper.getUserByName(registerUser.getName());
        if (existingUser != null) {
            return null; // 用户名已存在，注册失败
        }
        // 2. 保存新用户到数据库
        registerUser.setId(IdUtil.simpleUUID().replace("-", ""));
        userMapper.insert(registerUser);
        return registerUser;
    }

}
