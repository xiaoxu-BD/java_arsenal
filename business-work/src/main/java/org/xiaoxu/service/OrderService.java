package org.xiaoxu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.domain.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    void createOrder(String name, Long userId, Long productId);

    List<Order> getOrderList();
}
