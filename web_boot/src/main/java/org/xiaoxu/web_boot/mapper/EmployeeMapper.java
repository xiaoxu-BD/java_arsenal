package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.web_boot.entity.domain.Employee;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeDTO;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {


    List<EmployeeDTO> getAllEmployees(Employee employee);
}
