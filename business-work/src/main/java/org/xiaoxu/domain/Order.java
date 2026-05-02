package org.xiaoxu.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

@TableName("t_order")
public class Order {
    @NotNull(message = "不能为null")
    private Long id;

    private Long productId;

    private Long userId;

    /**
    * 订单号
    */
    @Size(max = 128,message = "订单号最大长度要小于 128")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
    * 商品名称
    */
    @Size(max = 128,message = "商品名称最大长度要小于 128")
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    private Date createTime;

    private Date modifyTime;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}