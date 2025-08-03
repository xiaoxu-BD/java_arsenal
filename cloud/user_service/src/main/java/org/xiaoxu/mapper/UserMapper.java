package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Param;
import org.xiaoxu.entity.User;
import org.xiaoxu.entity.vo.UserVO;

/**
 * @className: UserMapper
 * @author: xiaoxu
 * @date: 2025/8/3 13:33
 * @Version: 1.0
 * @description:
 */

public interface UserMapper {
    UserVO getUserById(String id);
    UserVO getUserByUserName(String userName);
    User getUserByName(String userName);

    void insert(@Param("user") User registerUser);
}
