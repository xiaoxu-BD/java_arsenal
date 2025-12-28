package org.xiaoxu.domain;

import lombok.Data;

import java.util.List;

/**
 * @className: DeptDTO
 * @author: xiaoxu
 * @date: 2025/12/22 21:03
 * @Version: 1.0
 * @description:
 */
@Data
public class DeptDTO {

    private Long id;
    private String name;
    private List<Employee> employeeList;

}
