package org.xiaoxu.pojo.vo;

import lombok.Data;
import org.xiaoxu.pojo.PracticeOrderItem;
import org.xiaoxu.pojo.PracticeUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @className: OrderDetailVO
 * @description: 订单详情视图对象。
 *               对应两道经典练习:
 *               多对一 association —— 一个订单 belongs to 一个用户;
 *               一对多 collection —— 一个订单 has many 明细。
 */
@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer orderStatus;
    private LocalDateTime payTime;
    private String remark;
    private LocalDateTime createTime;

    /** 多对一:订单 -> 下单用户 */
    private PracticeUser user;

    /** 一对多:订单 -> 订单明细 */
    private List<PracticeOrderItem> items;
}
