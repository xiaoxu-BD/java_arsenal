package org.xiaoxu.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.workflow.entity.Department;

/**
 * 部门 Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
