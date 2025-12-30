package org.xiaoxu.web_boot.entity.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @className: Employee
 * @author: xiaoxu
 * @date: 2025/12/28 10:09
 * @Version: 1.0
 * @description: 数据库实体表
 */
@Getter
@Setter
public class Employee {

    private Long id;

    private String empNo;

    private String empName;

    private Long deptId;

    private Long positionId;

    private Integer status;

    private Date hireDate;

    private Date createTime;

    private Date updateTime;

}
