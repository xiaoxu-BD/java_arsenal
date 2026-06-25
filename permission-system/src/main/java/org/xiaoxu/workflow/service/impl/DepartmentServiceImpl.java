package org.xiaoxu.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xiaoxu.workflow.entity.Department;
import org.xiaoxu.workflow.entity.UserDepartment;
import org.xiaoxu.workflow.mapper.DepartmentMapper;
import org.xiaoxu.workflow.mapper.UserDepartmentMapper;
import org.xiaoxu.workflow.service.DepartmentService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final UserDepartmentMapper userDepartmentMapper;

    @Override
    public Department getByDeptCode(String deptCode) {
        return getOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getDeptCode, deptCode)
                .eq(Department::getDeleted, "0"));
    }

    @Override
    public List<Department> getDepartmentsByUsername(String username) {
        // 查询用户关联的部门ID列表
        List<UserDepartment> userDepts = userDepartmentMapper.selectList(
                new LambdaQueryWrapper<UserDepartment>()
                        .eq(UserDepartment::getUsername, username)
        );
        
        if (userDepts.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> deptIds = userDepts.stream()
                .map(UserDepartment::getDepartmentId)
                .collect(Collectors.toList());
        
        return listByIds(deptIds);
    }

    @Override
    public Department getPrimaryDepartment(String username) {
        // 查询用户的主部门关联
        UserDepartment userDept = userDepartmentMapper.selectOne(
                new LambdaQueryWrapper<UserDepartment>()
                        .eq(UserDepartment::getUsername, username)
                        .eq(UserDepartment::getIsPrimary, 1)
        );
        
        if (userDept == null) {
            // 如果没有主部门，返回第一个部门
            userDept = userDepartmentMapper.selectOne(
                    new LambdaQueryWrapper<UserDepartment>()
                            .eq(UserDepartment::getUsername, username)
            );
        }
        
        if (userDept == null) {
            return null;
        }
        
        return getById(userDept.getDepartmentId());
    }

    @Override
    public String getDepartmentManager(String deptCode) {
        Department dept = getByDeptCode(deptCode);
        if (dept == null) {
            log.warn("部门不存在: deptCode={}", deptCode);
            return null;
        }
        return dept.getManagerUsername();
    }
}
