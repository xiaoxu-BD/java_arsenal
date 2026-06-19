package org.xiaoxu.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 履约单 VO
 */
@Data
public class FulfillmentOrderVO implements Serializable {

    private Long id;
    private String orderNo;
    private String title;
    private String description;
    private String status;
    private String processInstId;
    private BigDecimal amount;
    private String applicant;
    private String remark;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
