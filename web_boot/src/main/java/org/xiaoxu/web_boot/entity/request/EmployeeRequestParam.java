package org.xiaoxu.web_boot.entity.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @className: EmployeeRequestParam
 * @author: xiaoxu
 * @date: 2025/12/28 10:20
 * @Version: 1.0
 * @description:
 */
@Getter
@Setter
public class EmployeeRequestParam {

    private String empName;

    private Long deptId;

    private Long positionId;

    private String status;

    private Long projectId;

    private Integer pageNum;

    private Integer pageSize;

}
