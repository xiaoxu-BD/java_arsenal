package org.xiaoxu.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建请假单 DTO
 */
@Data
public class ApproveLeaveCreateDTO {

    @NotBlank(message = "请假原因不能为空")
    private String leaveReason;

    @NotNull(message = "请假天数不能为空")
    private BigDecimal leaveDay;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String leaveType;
}
