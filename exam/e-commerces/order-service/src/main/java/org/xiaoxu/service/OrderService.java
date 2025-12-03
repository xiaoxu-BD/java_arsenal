package org.xiaoxu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.dto.CreateOrderRequest;
import org.xiaoxu.pojo.OrderEntity;

/**
 * @className: OrderService
 * @author: xiaoxu
 * @date: 2025/11/29 9:17
 * @Version: 1.0
 * @description:
 */
public interface OrderService extends IService<OrderEntity> {


    String createOrder(CreateOrderRequest request);


    public void updateOrderStatus(String orderId, String status);
}
