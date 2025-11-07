package org.xiaoxu.web_boot.service.practice;

import org.xiaoxu.web_boot.entity.practice.Order;
import org.xiaoxu.web_boot.entity.practice.OrderSummary;

import java.util.List;

public interface OrderService {

    public List<OrderSummary> processOrders(List<Order> request);
}
