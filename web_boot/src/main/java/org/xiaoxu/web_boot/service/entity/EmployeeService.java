package org.xiaoxu.web_boot.service.entity;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.web_boot.entity.domain.Employee;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeDTO;

import java.util.List;

/**
 * @className: EmployeeService
 * @author: xiaoxu
 * @date: 2025/12/28 10:29
 * @Version: 1.0
 * @description:
 */
public interface EmployeeService  extends IService<Employee> {
    List<EmployeeDTO> getAllEmployees(Employee employee, Integer pageNum, Integer pageSize);
}
