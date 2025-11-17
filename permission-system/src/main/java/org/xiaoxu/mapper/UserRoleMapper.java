package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pojo.SystemUserRole;

import java.util.Set;

@Mapper
public interface UserRoleMapper extends BaseMapper<SystemUserRole> {



   Set<Long> getRoleIdsByUserId(Long userId);



}
