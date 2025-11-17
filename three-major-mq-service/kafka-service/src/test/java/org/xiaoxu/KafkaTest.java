package org.xiaoxu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.xiaoxu.producer.KafkaProducer;

@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void testSend() {
        kafkaTemplate.send("demo_topic", "你好 Kafka，这是 SpringBoot Test！");
        System.out.println("Kafka消息发送成功！");
    }

    @Autowired
    private KafkaProducer kafkaProducer;;
    @Test
    public void sendBypP(){
        kafkaProducer.send("demo_topic", "你好 Kafka，这是 SpringBoot Test！");
        System.out.println("Kafka消息发送成功！");
    }
}
