package org.xiaoxu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xiaoxu.entity.Role;
import org.xiaoxu.entity.User;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.res.UserDTO;
import org.xiaoxu.service.UserService;

import java.util.List;

/**
 * @className: UserServiceImpl
 * @author: xiaoxu
 * @date: 2025/10/6 16:27
 * @Version: 1.0
 * @description:
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {


    @Override
    public UserDTO getByUsername(String username) {
        return null;
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {

    }

    @Override
    public List<Role> listRolesByUser(Long userId) {
        return List.of();
    }
}
