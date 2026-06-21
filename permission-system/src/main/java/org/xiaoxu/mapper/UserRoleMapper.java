package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.pojo.SystemUserRole;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserRoleMapper extends BaseMapper<SystemUserRole> {

    Set<Long> getRoleIdsByUserId(Long userId);

    /**
     * 根据角色编码查询拥有该角色的用户名列表
     */
    List<String> getUsernamesByRoleCode(@Param("roleCode") String roleCode);
}
