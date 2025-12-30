package org.xiaoxu.web_boot.entity.domain;

import lombok.Data;

import java.util.Date;

/**
 * @className: EmployeeProject
 * @author: xiaoxu
 * @date: 2025/12/28 11:00
 * @Version: 1.0
 * @description:
 */
@Data
public class EmployeeProject {

    private Long id;

    private Long empId;

    private Long projectId;

    private String roleName;

    private Date createTime;

    private Date updateTime;
}
