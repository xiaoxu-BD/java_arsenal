package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xiaoxu.pojo.SystemRole;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<SystemRole> {

    /**
     * 根据 username 查询该用户拥有的角色 code 列表。
     * <p>
     * 工作流模块使用：BPMN candidateGroup == system_role.code，
     * 通过此方法拿到当前登录用户在系统中的角色 code，再去过滤 Flowable 待办。
     */
    @Select("SELECT r.code FROM system_role r " +
            "JOIN system_user_role ur ON ur.role_id = r.id " +
            "JOIN system_users u ON u.id = ur.user_id " +
            "WHERE u.username = #{username} " +
            "  AND r.deleted = '0' " +
            "  AND ur.deleted = '0' " +
            "  AND u.deleted = '0'")
    List<String> getRoleCodesByUsername(@Param("username") String username);
}
