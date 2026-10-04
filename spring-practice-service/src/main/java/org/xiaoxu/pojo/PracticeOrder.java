package org.xiaoxu.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @className: PracticeOrder
 * @description: 练习-订单表(practice_order),配合 XML mapper 教学
 */
@Data
public class PracticeOrder {
    private Long id;
    /** 业务订单号,形如 SO20261003008 */
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    /** 状态:0待支付 1已支付 2已发货 3已完成 4已取消 */
    private Integer orderStatus;
    /** 支付时间,未支付时为 NULL —— 动态SQL练习点 */
    private LocalDateTime payTime;
    /** 备注,可能为 NULL */
    private String remark;
    private LocalDateTime createTime;
}
