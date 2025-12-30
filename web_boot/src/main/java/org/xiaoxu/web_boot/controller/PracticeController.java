package org.xiaoxu.web_boot.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.entity.domain.Employee;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeDTO;
import org.xiaoxu.web_boot.entity.request.EmployeeRequestParam;
import org.xiaoxu.web_boot.service.entity.EmployeeService;
import org.xiaoxu.web_boot.utils.BeanUtil;

import java.util.List;

/**
 * @className: PracticeController
 * @author: xiaoxu
 * @date: 2025/12/28 10:36
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/practice/data")
public class PracticeController {

    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/get")
    public Result<List<EmployeeDTO>> getEmployee(@RequestBody EmployeeRequestParam requestParam){

        Employee employee = new Employee();
        employee.setEmpName(requestParam.getEmpName());

        BeanUtils.copyProperties(requestParam,employee);

        List<EmployeeDTO> result =  employeeService.getAllEmployees(employee,requestParam.getPageNum(),requestParam.getPageSize());

        return Result.success(result);


    }




}
