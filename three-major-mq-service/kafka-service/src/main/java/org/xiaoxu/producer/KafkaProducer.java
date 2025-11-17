package org.xiaoxu.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @className: ProducerKa
 * @author: xiaoxu
 * @date: 2025/11/15 20:26
 * @Version: 1.0
 * @description:
 */
@Component
 public class KafkaProducer {

        private final KafkaTemplate<String, String> kafkaTemplate;

        public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        public void send(String topic, String msg) {
            kafkaTemplate.send(topic, msg);
            System.out.println("发送成功：" + msg);
        }
    }


