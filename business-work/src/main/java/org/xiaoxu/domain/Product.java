package org.xiaoxu.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 商品表
 */
public class Product {
    @NotNull(message = "不能为null")
    private Long id;

    /**
    * 商品名称
    */
    @Size(max = 128,message = "商品名称最大长度要小于 128")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
    * 商品描述
    */
    @Size(max = 512,message = "商品描述最大长度要小于 512")
    private String memo;

    /**
    * 商品主图
    */
    @Size(max = 256,message = "商品主图最大长度要小于 256")
    private String priUrl;

    /**
    * 商品价格
    */
    @NotNull(message = "商品价格不能为null")
    private BigDecimal price;

    /**
    * 1-上架 0-下架
    */
    private Byte status;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    @TableField(fill = FieldFill.DEFAULT)
    private Byte deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getPriUrl() {
        return priUrl;
    }

    public void setPriUrl(String priUrl) {
        this.priUrl = priUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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

    public Byte getDeleted() {
        return deleted;
    }

    public void setDeleted(Byte deleted) {
        this.deleted = deleted;
    }
}