package org.xiaoxu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.domain.SendDTO;
import org.xiaoxu.producer.OrderService;
import org.xiaoxu.producer.ProduceRockerMQDEM;

@RestController
public class TestController {

    @Autowired
    private ProduceRockerMQDEM producer;
    @Autowired
    private OrderService orderService;

    @GetMapping("/send")
    public String send() {
        producer.send("hello rocketmq");
        return "ok";
    }


    @PostMapping("/order/send")
    public String sendOrder(@RequestBody SendDTO sendDTO) {
        orderService.createOrder(sendDTO.getEmail(), sendDTO.getOrderId());
        return "ok";
    }
}