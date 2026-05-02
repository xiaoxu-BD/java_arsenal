package org.xiaoxu.domain;

import java.util.Date;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
/**
 * 库存流水表
 */
public class StockLog {
    @NotNull(message = "不能为null")
    private Long id;

    /**
    * 商品ID
    */
    @NotNull(message = "商品ID不能为null")
    private Long productId;

    /**
    * 关联订单号
    */
    @Size(max = 64,message = "关联订单号最大长度要小于 64")
    private String orderNo;

    /**
    * 变动数量（正数加，负数减）
    */
    @NotNull(message = "变动数量（正数加，负数减）不能为null")
    private Integer quantity;

    /**
    * 1-秒杀扣减 2-下单扣减 3-取消回滚 4-手动调整
    */
    @NotNull(message = "1-秒杀扣减 2-下单扣减 3-取消回滚 4-手动调整不能为null")
    private Byte type;

    /**
    * 变动前剩余
    */
    private Integer beforeRemain;

    /**
    * 变动后剩余
    */
    private Integer afterRemain;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Integer getBeforeRemain() {
        return beforeRemain;
    }

    public void setBeforeRemain(Integer beforeRemain) {
        this.beforeRemain = beforeRemain;
    }

    public Integer getAfterRemain() {
        return afterRemain;
    }

    public void setAfterRemain(Integer afterRemain) {
        this.afterRemain = afterRemain;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}