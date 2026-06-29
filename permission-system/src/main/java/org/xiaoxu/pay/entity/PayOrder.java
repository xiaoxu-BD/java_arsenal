package org.xiaoxu.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款单实体
 */
@Data
@TableName("pay_order")
public class PayOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 付款单号 */
    private String orderNo;

    /** 业务类型：leave/fulfillment */
    private String businessType;

    /** 业务ID */
    private Long businessId;

    /** 流程实例ID */
    private String processInstanceId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 商品名称 */
    private String productName;

    /** 金额 */
    private BigDecimal amount;

    /** 状态：PENDING/PAID/CANCELLED/EXPIRED/REFUNDED */
    private String status;

    /** 支付宝交易号 */
    private String alipayTradeNo;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /** 取消原因 */
    private String cancelReason;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
