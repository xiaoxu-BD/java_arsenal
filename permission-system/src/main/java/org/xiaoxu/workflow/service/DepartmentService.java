package org.xiaoxu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.workflow.entity.Department;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<Department> {
    
    /**
     * 根据部门编码获取部门
     */
    Department getByDeptCode(String deptCode);
    
    /**
     * 获取用户的部门列表
     */
    List<Department> getDepartmentsByUsername(String username);
    
    /**
     * 获取用户的主部门
     */
    Department getPrimaryDepartment(String username);
    
    /**
     * 获取部门经理
     */
    String getDepartmentManager(String deptCode);
}
