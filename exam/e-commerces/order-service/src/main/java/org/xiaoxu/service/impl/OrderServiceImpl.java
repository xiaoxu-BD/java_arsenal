package org.xiaoxu.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.dto.CreateOrderRequest;
import org.xiaoxu.mapper.OrderMapper;
import org.xiaoxu.mapper.OutboxMapper;
import org.xiaoxu.pojo.OrderEntity;
import org.xiaoxu.pojo.OutboxMessageEntity;
import org.xiaoxu.service.OrderService;

import java.util.Map;
import java.util.UUID;

/**
 * @className: OrderServiceImpl
 * @author: xiaoxu
 * @date: 2025/11/29 9:22
 * @Version: 1.0
 * @description:
 */
@Service
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    private final OrderMapper orderMapper;

    private final ObjectMapper objectMapper;
    private final OutboxMapper outboxMapper;

    private final String ORDER_CREATED_STATUS = "PENDING";


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(CreateOrderRequest request) {
        String orderId = String.valueOf(IdUtil.getSnowflakeNextId());

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setStatus(ORDER_CREATED_STATUS);
        order.setUserId(request.getUserId());
        try {
            order.setPayload(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 订单落库
        orderMapper.insert(order);


        // 发送消息
        OutboxMessageEntity msg = new OutboxMessageEntity();

        msg.setAggregateType("order");
        msg.setAggregateId(orderId);
        msg.setEventType("order.created");
        try {
            msg.setPayload(objectMapper.writeValueAsString(Map.of(
                    "order_id", orderId,
                    "user_id", request.getUserId(),
                    "items", request.getItems()
            )));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        outboxMapper.insert(msg);

        return orderId;
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        OrderEntity order = this.getById(orderId);
        if (order != null){
            order.setStatus(status);
            orderMapper.updateById(order);
        }

    }


    public static void main(String[] args) {
        String orderId = String.valueOf(IdUtil.getSnowflakeNextId());
        System.out.println(orderId);
    }
}
