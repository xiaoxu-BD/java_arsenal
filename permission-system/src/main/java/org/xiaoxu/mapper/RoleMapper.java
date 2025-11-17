package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pojo.SystemRole;

@Mapper
public interface RoleMapper extends BaseMapper<SystemRole> {
}
