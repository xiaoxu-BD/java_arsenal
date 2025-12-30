package org.xiaoxu.web_boot.service.entity.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.entity.domain.Employee;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeDTO;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeProjectDTO;
import org.xiaoxu.web_boot.mapper.EmployeeMapper;
import org.xiaoxu.web_boot.service.entity.EmployeeProjectService;
import org.xiaoxu.web_boot.service.entity.EmployeeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className: EmployeeServiceImpl
 * @author: xiaoxu
 * @date: 2025/12/28 10:31
 * @Version: 1.0
 * @description:
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{

    @Autowired
    private  EmployeeMapper employeeMapper;

    @Autowired
    private EmployeeProjectService employeeProjectService;

    @Override
    public List<EmployeeDTO> getAllEmployees(Employee employee, Integer pageNum, Integer pageSize) {
        List<EmployeeDTO> allEmployees = employeeMapper.getAllEmployees(employee);
        List<Long> empIds = allEmployees.stream().map(EmployeeDTO::getId).toList();
        List<EmployeeProjectDTO> empProjects  = employeeProjectService.listByEmpIds(empIds);

        Map<Long, List<String>> projectMap = empProjects.stream()
                .collect(Collectors.groupingBy(EmployeeProjectDTO::getEmpId,
                        Collectors.mapping(EmployeeProjectDTO::getProjectName, Collectors.toList())));

        List<EmployeeDTO> result = new ArrayList<>();

        for (EmployeeDTO allEmployee : allEmployees) {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(allEmployee.getId());
            employeeDTO.setProjectNames(projectMap.get(allEmployee.getId()));
            employeeDTO.setEmpName(allEmployee.getEmpName());
            employeeDTO.setEmpNo(allEmployee.getEmpNo());
            employeeDTO.setDeptName(allEmployee.getDeptName());
            employeeDTO.setPositionName(allEmployee.getPositionName());
            result.add(employeeDTO);
        }

        return result ;
    }
}
