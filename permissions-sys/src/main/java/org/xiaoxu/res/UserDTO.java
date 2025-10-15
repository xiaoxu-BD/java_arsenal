package org.xiaoxu.res;

import lombok.Data;
import org.xiaoxu.entity.Permission;
import org.xiaoxu.entity.Role;

import java.util.List;

/**
 * @className: UserDTO
 * @author: xiaoxu
 * @date: 2025/10/6 16:28
 * @Version: 1.0
 * @description:
 */
@Data
public class UserDTO {

    private Long id;
    private String username;
    private String nickname;
    private String enabled;
    private List<Role> roles;
    private List<Permission> permissions;
}
