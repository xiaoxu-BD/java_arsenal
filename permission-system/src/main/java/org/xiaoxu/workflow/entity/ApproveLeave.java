package org.xiaoxu.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 请假审批实体
 *
 * <p>映射表 approve_leave。审计字段命名遵循表实际列：create_time / update_time / deleted（VARCHAR）。
 * <p>{@code modifyTime} 和 {@code deleted} 加 {@code @TableField(exist = false)}：仅用于满足
 * DataObjectHandler 的反射调用（它按字段名 modifyTime/deleted 自动填值），不会写入数据库。
 */
@Data
@TableName("approve_leave")
public class ApproveLeave {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userName;

    private String leaveReason;

    private BigDecimal leaveDay;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private String processInstanceId;

    private String leaveType;

    private String identifier;

    private String status;

    /** 创建人（业务代码手动 set） */
    private String creator;

    /** 创建时间（DataObjectHandler 自动填充，类型必须是 java.util.Date） */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    private String updater;

    /** 更新时间（DB ON UPDATE CURRENT_TIMESTAMP 自动维护） */
    private Date updateTime;

    /** DataObjectHandler 占位字段：按字段名 modifyTime 填值，不写库 */
    @TableField(exist = false)
    private Date modifyTime;

    /** DataObjectHandler 占位字段：DB 列 deleted 有 DEFAULT '0'，由数据库填 */
    @TableField(exist = false)
    private Integer deleted;
}
