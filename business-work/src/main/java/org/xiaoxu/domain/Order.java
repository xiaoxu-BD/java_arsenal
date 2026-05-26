package org.xiaoxu.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.xiaoxu.enums.OrderStatusEnum;
import org.xiaoxu.handler.OrderStatusTypeHandler;

import java.util.Date;

// autoResultMap = true 是关键！
// 不加的话，MyBatis-Plus 内置方法（selectById, insert 等）会忽略 @TableField 中的 typeHandler 配置
// 因为 MyBatis-Plus 默认用简单的 ResultMap，不会为 typeHandler 生成自定义映射
@TableName(value = "t_order", autoResultMap = true)
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

    /**
     * 订单状态（枚举类型）
     *
     * typeHandler 指定 MyBatis 用哪个 TypeHandler 来做 Java枚举 ↔ 数据库Integer 的转换：
     * - 写入时：调用 TypeHandler.setNonNullParameter()，将枚举的 code 存入数据库
     * - 读取时：调用 TypeHandler.getNullableResult()，将数据库的 Integer 转为枚举对象
     *
     * 这个注解只在 autoResultMap = true 时才生效（针对 MyBatis-Plus 内置方法）
     * 对于自定义 XML 查询，需要在 resultMap 中单独声明 typeHandler
     */
    @TableField(typeHandler = OrderStatusTypeHandler.class)
    private OrderStatusEnum status;

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

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }
}