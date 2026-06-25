package org.xiaoxu.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.workflow.entity.UserDepartment;

/**
 * 用户部门关联 Mapper
 */
@Mapper
public interface UserDepartmentMapper extends BaseMapper<UserDepartment> {
}
