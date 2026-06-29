package org.xiaoxu.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 */
@Data
@TableName("pay_record")
public class PayRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 付款单号 */
    private String orderNo;

    /** 支付宝交易号 */
    private String alipayTradeNo;

    /** 交易状态 */
    private String tradeStatus;

    /** 交易金额 */
    private BigDecimal totalAmount;

    /** 买家ID */
    private String buyerId;

    /** 原始回调数据 */
    private String rawData;

    /** 创建时间 */
    private LocalDateTime createTime;
}
