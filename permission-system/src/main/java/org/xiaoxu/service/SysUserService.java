package org.xiaoxu.service;

import org.springframework.beans.factory.annotation.Autowired;
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



    public List<SystemUsers> getAll(){
        List<SystemUsers> systemUsers = userMapper.selectList(null);
        if (!CollectionUtils.isEmpty(systemUsers)){
            return systemUsers;
        }
        return null;
    }
}
