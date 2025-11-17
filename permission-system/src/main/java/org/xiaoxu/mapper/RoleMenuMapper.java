package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pojo.SystemRoleMenu;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper
public interface RoleMenuMapper extends BaseMapper<SystemRoleMenu> {



    Set<Long> getMenuIdsByRoleId(Collection<Long> roleIds);
}
