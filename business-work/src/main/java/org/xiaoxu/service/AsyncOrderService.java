package org.xiaoxu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncOrderService {

    private final OrderService orderService;

    @Async
    public void asyncCreateOrder(Long userId, Long productId) {
        log.info("【Async方法】开始执行，线程: {}", Thread.currentThread().getName());
        orderService.createOrder("异步订单", userId, productId);
        log.info("【Async方法】订单创建完成");
    }
}