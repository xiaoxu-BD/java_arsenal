package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pojo.SystemMenu;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper
public interface MenuMapper extends BaseMapper<SystemMenu> {

    Set<String> getPermissionCodeByMenuIds(Collection<Long> menuIds);

    Set<String> getPermissionCodeByUserId(Long id);
}
