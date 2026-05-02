package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.chainofresboot.OrderCreateValidator;
import org.xiaoxu.chainofresboot.request.OrderCreateRequest;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderCreateValidator orderValidatorChain;

    public OrderController(OrderCreateValidator orderValidatorChain) {
        this.orderValidatorChain = orderValidatorChain;
    }

    @PostMapping("/create")
    public String createOrder(@RequestBody OrderCreateRequest request) {

        orderValidatorChain.validate(request);

        return "订单创建成功";
    }
}