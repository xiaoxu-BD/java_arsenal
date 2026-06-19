package org.xiaoxu.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 请假单 VO
 */
@Data
public class ApproveLeaveVO implements Serializable {

    private Long id;
    private String userName;
    private String status;
    private String leaveReason;
    private String processInstanceId;
    private BigDecimal leaveDay;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String leaveType;
    private String identifier;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
