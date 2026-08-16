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

    @GetMapping("/send/poison")
    public String sendPoison() {
        producer.sendPoison();
        return "毒消息已发出：预期 ~10 秒后重试一次，再失败进死信；盯着应用日志和 %RETRY%/%DLQ% 两个 topic";
    }

    @GetMapping("/send/async")
    public String sendAsync() {
        producer.sendAsync("hello-async-" + System.currentTimeMillis());
        return "ok（接口此刻已返回，broker 确认和消费在别的线程，注意日志顺序）";
    }


    @PostMapping("/order/send")
    public String sendOrder(@RequestBody SendDTO sendDTO) {
        orderService.createOrder(sendDTO.getEmail(), sendDTO.getOrderId());
        return "ok";
    }
}