package org.xiaoxu.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @className: PracticeOrderItem
 * @description: 练习-订单明细表(practice_order_item),配合 XML mapper 教学
 */
@Data
public class PracticeOrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    /** 商品名快照:下单时冗余存储,防止商品后来改名/删除影响历史订单展示 */
    private String productName;
    private Integer quantity;
    /** 成交单价快照 */
    private BigDecimal unitPrice;
}
