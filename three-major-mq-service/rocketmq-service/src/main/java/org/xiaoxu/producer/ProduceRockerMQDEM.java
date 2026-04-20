package org.xiaoxu.producer;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @className: ProduceRockerMQDEM
 * @author: xiaoxu
 * @date: 2026/4/18 23:25
 * @Version: 1.0
 * @description:
 */
@Service
public class ProduceRockerMQDEM {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    public void send(String msg) {
        rocketMQTemplate.convertAndSend("rocket-service-exm", msg);
    }
}
