package org.xiaoxu.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.Users;

/**
 * @className: UserServiceImpl
 * @author: xiaoxu
 * @date: 2025/12/3 20:08
 * @Version: 1.0
 * @description:
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService{


    @Autowired
    private UserMapper userMapper;


}
