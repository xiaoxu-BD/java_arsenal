package org.xiaoxu.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;
import org.xiaoxu.enums.UserStatusEnum;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @className: UserTypeHandler
 * @author: xiaoxu
 * @date: 2025/12/3 20:18
 * @Version: 1.0
 * @description:
 */
@Component
public class UserTypeHandler extends BaseTypeHandler<UserStatusEnum> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getCode());
    }

    @Override
    public UserStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName); //从数据库中获取的设置的枚举值的字段值
        return UserStatusEnum.fromCode(code);
    }

    @Override
    public UserStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return UserStatusEnum.fromCode(code);
    }

    @Override
    public UserStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return UserStatusEnum.fromCode(code);
    }
}
