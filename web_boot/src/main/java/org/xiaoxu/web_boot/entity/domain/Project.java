package org.xiaoxu.web_boot.entity.domain;

import lombok.Data;

import java.util.Date;

/**
 * @className: Project
 * @author: xiaoxu
 * @date: 2025/12/28 11:08
 * @Version: 1.0
 * @description:
 */
@Data
public class Project {
    private Long id;
    private String projectName;

    private String projectCode;
    private Date startDate;

    private Date endDate;

    private Date createTime;
    private  Date updateTime;

}
