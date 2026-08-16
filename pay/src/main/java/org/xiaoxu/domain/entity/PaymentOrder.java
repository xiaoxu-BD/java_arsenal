package org.xiaoxu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xiaoxu.enums.PayMethod;
import org.xiaoxu.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// entity/PaymentOrder.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderId;
    private String userId;
    private BigDecimal amount;

    @EnumValue                        // MP 自动把枚举 name() 存入数据库
    private PayMethod payMethod;

    @EnumValue
    private PaymentStatus status;

    private String transactionId;
    private String description;
    private String failReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime payTime;

    @TableLogic                       // 逻辑删除
    private Integer deleted;
}