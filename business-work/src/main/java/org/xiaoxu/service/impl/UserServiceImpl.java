package org.xiaoxu.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaoxu.domain.Users;
import org.xiaoxu.mapper.UsersMapper;
import org.xiaoxu.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UsersMapper usersMapper;
    @Override
    public boolean verified(Long userId) {

        Users user = usersMapper.selectByPrimaryKey(userId);
        assert user != null;
        return user.getStatus() == 1;
    }
}
