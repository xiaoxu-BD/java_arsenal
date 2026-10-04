package org.xiaoxu.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @className: UserOrderStatVO
 * @description: 用户订单统计视图对象,GROUP BY 聚合查询的接收器。
 */
@Data
public class UserOrderStatVO {
    private Long userId;
    private String username;
    /** 订单数,对应 COUNT(*) */
    private Integer orderCount;
    /** 累计金额,对应 SUM(total_amount) */
    private BigDecimal totalAmount;
}
