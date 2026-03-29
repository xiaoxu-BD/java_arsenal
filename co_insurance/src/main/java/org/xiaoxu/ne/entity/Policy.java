package org.xiaoxu.ne.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 主保单（对应 insurance_policy）
 *
 * 说明：
 * 1. 主单承载完整业务信息（总保费、总保额等）
 * 2. splitStatus 表示拆单聚合状态
 */
@Data
public class Policy {

    /** 主键ID（雪花算法） */
    private Long policyId;

    /** 主保单号（全局唯一，对外展示） */
    private String policyNo;

    /** 总保险金额 */
    private BigDecimal totalAmount;

    /** 总保费（拆单基准） */
    private BigDecimal totalPremium;

    /**
     * 拆单状态：
     * NONE：非共保
     * PENDING：待拆单（异步场景）
     * SPLIT：已拆单（子单已生成）
     * ALL_ACTIVE：全部分保单生效
     * PARTIAL_ACTIVE：部分生效
     * EXCEPTION：异常状态
     */
    private String splitStatus;

    /** 创建时间 */
    private LocalDateTime createdAt;
}