package org.xiaoxu;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.domain.Order;
import org.xiaoxu.mapper.OrderMapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unit test for simple App.
 */
@SpringBootTest
class OrderTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void testInsert() {
        Order order = new Order();
        order.setUserId(3L);
        order.setAmount(new BigDecimal("99.99"));
        orderMapper.insert(order);
    }

    @Test
    void testQuery() {
        List<Order> list = orderMapper.listByUserId(3L);
        list.forEach(System.out::println);
    }
}
