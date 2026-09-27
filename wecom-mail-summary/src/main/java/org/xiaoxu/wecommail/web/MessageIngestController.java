package org.xiaoxu.wecommail.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.common.ApiResponse;
import org.xiaoxu.wecommail.consumer.WechatMessageConsumer;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MQ 消息接入(接入节点二:业务方发送消息后,MQ 把消息实体推给我们)。
 *
 * <p>MQ 产品/Topic 尚未最终确定(设计文档决策点 D1)。确定后新增对应 Listener
 * (RocketMQ @RocketMQMessageListener / RabbitMQ @RabbitListener / Kafka @KafkaListener)
 * 回调 {@link WechatMessageConsumer#onMessage} 即可,消费逻辑无需改动。
 * 本接口与 Listener 调用同一消费逻辑,真实校验 + 幂等落库,保留用于联调与消息补录。
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageIngestController {

    private final WechatMessageConsumer consumer;

    /** 推送一条消息实体(原文 JSON,字段结构与 MQ 消息一致),按字节接收后显式 UTF-8 解码 */
    @PostMapping
    public ApiResponse<Map<String, Object>> publish(@RequestBody byte[] rawBytes) {
        String rawBody = new String(rawBytes, StandardCharsets.UTF_8);
        WechatMessageConsumer.ConsumeResult result = consumer.onMessage(rawBody);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("consumeResult", result.name());
        data.put("desc", result.getDesc());
        return ApiResponse.ok(data);
    }
}
