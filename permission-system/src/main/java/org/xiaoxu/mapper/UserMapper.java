package org.xiaoxu.mapper;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.common.convert.UserConvert;
import org.xiaoxu.common.excepiton.user.AuthErrorCode;
import org.xiaoxu.common.excepiton.user.UserException;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.dto.SystemUserDTO;

@Mapper
public interface UserMapper extends BaseMapper<SystemUsers> {




    default SystemUserDTO getOne(String username){
        SystemUsers systemUsers = this.selectOne(new LambdaQueryWrapper<SystemUsers>().eq(SystemUsers::getUsername, username).eq(SystemUsers::getDeleted, false));
        Assert.notNull(systemUsers,()-> new UserException(AuthErrorCode.USER_NAME_NOT_EXIST));
        return   UserConvert.INSTANCE.mapToUser(systemUsers);

    }

    /**
     * 根据邮箱查询用户（排除已删除）
     */
    default SystemUsers findByEmail(String email) {
        return this.selectOne(new LambdaQueryWrapper<SystemUsers>()
                .eq(SystemUsers::getEmail, email)
                .eq(SystemUsers::getDeleted, "0"));
    }
}
