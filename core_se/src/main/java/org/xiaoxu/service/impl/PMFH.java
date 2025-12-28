package org.xiaoxu.service.impl;

import org.xiaoxu.domain.Department;
import org.xiaoxu.domain.DeptDTO;
import org.xiaoxu.domain.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className: PMFH
 * @author: xiaoxu
 * @date: 2025/12/22 21:01
 * @Version: 1.0
 * @description:
 */
public class PMFH {



    public List<DeptDTO> getResult(){

        //主对象是:DeptDTO 只允许在里面set
        //其他的要set全部变成map
        List<Employee> employees = this.getEmployees();
        List<Department> departments = this.getDepartments();
        Map<Long, List<Employee>> empMap = employees.stream().collect(Collectors.groupingBy(Employee::getDepartmentId));
        List<DeptDTO> result = new ArrayList<>();
        for (Department department : departments) {
            DeptDTO dto  = new DeptDTO();
            dto.setId(department.getId());
            dto.setName(department.getName());
            dto.setEmployeeList(empMap.get(department.getId()));
            result.add(dto);
        }
        return result;
    }



    // 模拟查询出来所有部门信息的集合
    public List<Department> getDepartments() {
        // 模拟查询数据库
        return List.of();
    }

    // 模拟查询出来所有员工信息的集合

    public List<Employee> getEmployees(){
        return List.of();
    }
}
