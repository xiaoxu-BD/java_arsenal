package org.xiaoxu.web_boot.entity.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @className: EmployeeDTO
 * @author: xiaoxu
 * @date: 2025/12/28 10:09
 * @Version: 1.0
 * @description:
 */
@Getter
@Setter
public class EmployeeDTO {
    private Long id;
    private String empNo;
    private String empName;
    private String deptName;
    private String positionName;
    //多对多
    private List<String> projectNames;
}
