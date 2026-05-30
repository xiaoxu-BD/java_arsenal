package org.xiaoxu.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xiaoxu.enums.OrderStatusEnum;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 订单状态枚举 TypeHandler
 *
 * 核心作用：在 Java 枚举 和 数据库 Integer 之间做双向转换
 *
 * 写入方向（Java → DB）：
 *   调用 setNonNullParameter()，将枚举的 code (Integer) 写入数据库
 *
 * 读取方向（DB → Java）：
 *   调用 getNullableResult()，从数据库读取 Integer code，再通过 fromCode() 转为枚举对象
 *   拿到枚举后，Java 代码中调用 .getDesc() 即可获取中文描述
 *
 * 注意：这里没有加 @Component，因为我们通过 @TableField(typeHandler=...) 或 XML resultMap 来指定
 *       如果加了 @MappedTypes + @MappedJdbcTypes 并配合 type-handlers-package 配置，也可以自动扫描注册
 */
public class OrderStatusTypeHandler extends BaseTypeHandler<OrderStatusEnum> {

    /**
     * 写入：将枚举 code 设置到 PreparedStatement
     * 例：INSERT INTO t_order (status) VALUES (?)  →  ? 会被替换为 0, 1, 2...
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OrderStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    /**
     * 读取（按列名）：从 ResultSet 中根据列名获取值
     * 例：SELECT status FROM t_order WHERE id = 1  →  拿到 status 列的整数值 → 转为枚举
     */
    @Override
    public OrderStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        // rs.getInt() 在数据库值为 NULL 时返回 0，需要用 wasNull() 判断是否真的为 NULL
        return rs.wasNull() ? null : OrderStatusEnum.fromCode(code);
    }

    /**
     * 读取（按列索引）：从 ResultSet 中根据列索引获取值
     */
    @Override
    public OrderStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return rs.wasNull() ? null : OrderStatusEnum.fromCode(code);
    }

    /**
     * 读取（存储过程）：从 CallableStatement 中获取值
     */
    @Override
    public OrderStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return cs.wasNull() ? null : OrderStatusEnum.fromCode(code);
    }
}
