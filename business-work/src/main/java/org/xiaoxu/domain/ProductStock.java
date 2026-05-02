package org.xiaoxu.domain;

import java.util.Date;
import jakarta.validation.constraints.NotNull;

/**
 * 库存表
 */
public class ProductStock {
    @NotNull(message = "不能为null")
    private Long id;

    /**
    * 商品ID
    */
    @NotNull(message = "商品ID不能为null")
    private Long productId;

    /**
    * 总库存
    */
    @NotNull(message = "总库存不能为null")
    private Integer total;

    /**
    * 已售数量
    */
    @NotNull(message = "已售数量不能为null")
    private Integer saled;

    /**
    * 剩余库存 = total - saled
    */
    @NotNull(message = "剩余库存 = total - saled不能为null")
    private Integer remain;

    /**
    * 乐观锁版本号
    */
    @NotNull(message = "乐观锁版本号不能为null")
    private Integer version;

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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSaled() {
        return saled;
    }

    public void setSaled(Integer saled) {
        this.saled = saled;
    }

    public Integer getRemain() {
        return remain;
    }

    public void setRemain(Integer remain) {
        this.remain = remain;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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