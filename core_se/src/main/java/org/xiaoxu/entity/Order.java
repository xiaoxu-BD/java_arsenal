package org.xiaoxu.entity;

import lombok.Getter;
import lombok.Setter;
import org.xiaoxu.se.enums.OrderStatusEnum;

import java.util.Date;

@Getter
@Setter
public class Order {
    private Long id;

    private Long productId;

    private Long userId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商品名称
     */
    private String productName;

    private Date createTime;

    private Date modifyTime;

    private OrderStatusEnum status;

}