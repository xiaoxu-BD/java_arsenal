package org.xiaoxu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.domain.Order;
import org.xiaoxu.mapper.OrderMapper;
import org.xiaoxu.service.OrderService;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void createOrder(String name, Long userId, Long productId) {
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-"," "));
        order.setUserId(userId);
        order.setProductName(name);
        order.setProductId(productId);
        orderMapper.insert(order);
    }

    @Override
    public List<Order> getOrderList() {

        return orderMapper.getList();
    }
}
