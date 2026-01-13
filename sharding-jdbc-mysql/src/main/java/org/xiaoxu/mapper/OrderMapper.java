package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.xiaoxu.domain.Order;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("""
        INSERT INTO t_order(user_id, amount, create_time)
        VALUES (#{userId}, #{amount}, NOW())
    """)
    int insert(Order order);

    @Select("""
        SELECT * FROM t_order WHERE user_id = #{userId}
    """)
    List<Order> listByUserId(Long userId);
}
