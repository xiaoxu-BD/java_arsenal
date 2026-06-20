package org.xiaoxu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.mapper.UserRoleMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.SystemUserRole;

import java.util.List;
import java.util.UUID;

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

    @Resource
    private UserRoleMapper userRoleMapper;

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

    /**
     * 根据 ID 查询用户（排除已删除）
     */
    public SystemUsers getUserById(Long id) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                        .eq(SystemUsers::getId, id)
                        .eq(SystemUsers::getDeleted, "0"));
    }

    /**
     * 更新个人资料（昵称、邮箱、手机、性别）
     */
    public void updateProfile(SystemUsers update) {
        userMapper.update(null,
                new LambdaUpdateWrapper<SystemUsers>()
                        .eq(SystemUsers::getId, update.getId())
                        .set(SystemUsers::getNickname, update.getNickname())
                        .set(SystemUsers::getEmail, update.getEmail())
                        .set(SystemUsers::getMobile, update.getMobile())
                        .set(SystemUsers::getSex, update.getSex()));
    }

    /**
     * 修改密码（验证旧密码）
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SystemUsers user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 首次登录跳过旧密码校验
        if (!"1".equals(user.getFirstLogin())) {
            if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("旧密码不正确");
            }
        }
        userMapper.update(null,
                new LambdaUpdateWrapper<SystemUsers>()
                        .eq(SystemUsers::getId, userId)
                        .set(SystemUsers::getPassword, passwordEncoder.encode(newPassword))
                        .set(SystemUsers::getFirstLogin, "0"));
    }

    /**
     * 管理员重置密码（不需要旧密码）
     */
    public void resetPassword(Long userId, String newPassword) {
        SystemUsers user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.update(null,
                new LambdaUpdateWrapper<SystemUsers>()
                        .eq(SystemUsers::getId, userId)
                        .set(SystemUsers::getPassword, passwordEncoder.encode(newPassword))
                        .set(SystemUsers::getFirstLogin, "0"));
    }

    /**
     * 更新头像 URL
     */
    public void updateAvatar(Long userId, String avatarUrl) {
        userMapper.update(null,
                new LambdaUpdateWrapper<SystemUsers>()
                        .eq(SystemUsers::getId, userId)
                        .set(SystemUsers::getAvatar, avatarUrl));
    }

    /**
     * 根据邮箱查询用户
     */
    public SystemUsers findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    /**
     * 通过邮箱自动注册（默认普通用户角色 role_id=2）
     */
    public SystemUsers createByEmail(String email) {
        SystemUsers user = new SystemUsers();
        // username 取邮箱前缀，如果重复则加随机后缀
        String baseUsername = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        String username = baseUsername;
        if (userMapper.selectOne(new LambdaQueryWrapper<SystemUsers>()
                .eq(SystemUsers::getUsername, username)) != null) {
            username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 6);
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setNickname(username);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString().substring(0, 8)));
        user.setStatus(0L);
        user.setDeleted("0");
        user.setFirstLogin("1");
        userMapper.insert(user);

        // 分配默认普通用户角色 (id=2)
        SystemUserRole userRole = new SystemUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L);
        userRole.setDeleted("0");
        userRoleMapper.insert(userRole);

        return user;
    }
}
