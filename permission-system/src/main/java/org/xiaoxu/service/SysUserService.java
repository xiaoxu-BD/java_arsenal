package org.xiaoxu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.SystemUsers;

import java.util.List;

/**
 * @className: SysUserService
 * @author: xiaoxu
 * @date: 2026/3/29 12:22
 * @Version: 1.0
 * @description:
 */
@Service
public class SysUserService {



    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<SystemUsers> getAll(){
        List<SystemUsers> systemUsers = userMapper.selectList(
                new LambdaQueryWrapper<SystemUsers>()
                        .eq(SystemUsers::getDeleted, "0")
        );
        if (!CollectionUtils.isEmpty(systemUsers)){
            return systemUsers;
        }
        return null;
    }

    public void createUser(SystemUsers user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    public void deleteUser(Long id) {
        SystemUsers user = new SystemUsers();
        user.setId(id);
        user.setDeleted("1");
        userMapper.updateById(user);
    }
}
