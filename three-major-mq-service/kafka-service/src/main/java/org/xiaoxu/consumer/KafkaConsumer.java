package org.xiaoxu.consumer;

/**
 * @className: KafkaConsumer
 * @author: xiaoxu
 * @date: 2025/11/15 20:27
 * @Version: 1.0
 * @description:
 */
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "demo_topic", groupId = "demo_group")
    public void listen(String msg) {
        System.out.println("收到消息：" + msg);
    }
}
