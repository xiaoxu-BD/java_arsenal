package org.xiaoxu.service;

import org.apache.ibatis.annotations.Param;
import org.xiaoxu.entity.User;

public interface UserService {

    User login(String username, String password);

    User register(@Param("user") User registerUser);
}
