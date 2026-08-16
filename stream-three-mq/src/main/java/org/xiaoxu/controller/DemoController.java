package org.xiaoxu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 命令式发送入口：StreamBridge 把消息送到指定输出绑定，
 * 代码里不出现任何 RocketMQ/RabbitMQ 的 API。
 */
@RestController
@RequestMapping("/stream")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final StreamBridge streamBridge;

    public DemoController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /** 发往 RocketMQ：order-out-0 → stream-order-topic */
    @GetMapping("/order")
    public String order(@RequestParam(name = "body", defaultValue = "hello-stream") String body) {
        boolean sent = streamBridge.send("order-out-0",
                MessageBuilder.withPayload(body)
                        .setHeader("bizKey", "ORDER-" + UUID.randomUUID().toString().substring(0, 8))
                        .build());
        log.info("[发送→RocketMQ] body={} 发送结果={}", body, sent);
        return sent ? "已发往 RocketMQ (stream-order-topic)" : "发送失败";
    }

    /** 发往 RabbitMQ：mail-out-0 → stream-mail exchange */
    @GetMapping("/mail")
    public String mail(@RequestParam(name = "body", defaultValue = "hello-stream") String body) {
        boolean sent = streamBridge.send("mail-out-0",
                MessageBuilder.withPayload(body)
                        .setHeader("bizKey", "MAIL-" + UUID.randomUUID().toString().substring(0, 8))
                        .build());
        log.info("[发送→RabbitMQ] body={} 发送结果={}", body, sent);
        return sent ? "已发往 RabbitMQ (stream-mail exchange)" : "发送失败";
    }

    /** 跨 binder：发往 RocketMQ 的 stream-notify-topic，notifyProcess 函数消费加工后自动发往 RabbitMQ。
     *  必须发到独立的输出绑定 notify-out-0——发到函数的输入绑定名(notifyProcess-in-0)会进程内短路绕过 broker */
    @GetMapping("/notify")
    public String notifyCross(@RequestParam(name = "body", defaultValue = "cross-binder-msg") String body) {
        boolean sent = streamBridge.send("notify-out-0", body);
        log.info("[发送→跨binder链路] body={} 发送结果={}，等待函数加工后自动流向 RabbitMQ", body, sent);
        return sent ? "已进入跨 binder 链路：RocketMQ → notifyProcess → RabbitMQ" : "发送失败";
    }
}
