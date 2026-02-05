package org.xiaoxu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @className: Employee
 * @author: xiaoxu
 * @date: 2025/12/22 21:00
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
public class Employee {
    private Long id;
    private Long departmentId;
    private String name;
}
