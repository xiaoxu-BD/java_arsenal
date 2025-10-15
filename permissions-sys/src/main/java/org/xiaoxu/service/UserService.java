package org.xiaoxu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.entity.Role;
import org.xiaoxu.entity.User;
import org.xiaoxu.res.UserDTO;

import java.util.List;

/**
 * @className: UserService
 * @author: xiaoxu
 * @date: 2025/10/6 16:26
 * @Version: 1.0
 * @description:
 */
public interface UserService extends IService<User> {

    UserDTO getByUsername(String username);
    void assignRoles(Long userId, List<Long> roleIds);
    List<Role> listRolesByUser(Long userId);
}
