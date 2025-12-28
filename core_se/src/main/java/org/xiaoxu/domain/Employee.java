package org.xiaoxu.domain;

import lombok.Data;

/**
 * @className: Employee
 * @author: xiaoxu
 * @date: 2025/12/22 21:00
 * @Version: 1.0
 * @description:
 */
@Data
public class Employee {
    private Long id;
    private Long departmentId;
    private String name;
}
