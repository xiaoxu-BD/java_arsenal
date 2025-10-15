package org.xiaoxu.producer;

import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;
import org.xiaoxu.message.Demo1Message;

/**
 * @className: Demo01Producer
 * @author: xiaoxu
 * @date: 2025/10/4 22:11
 * @Version: 1.0
 * @description:
 */
@Component
public class Demo01Producer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;


    public SendResult syncSend(Integer id){
        Demo1Message message = new Demo1Message();
        message.setId(id);
        return rocketMQTemplate.syncSend(Demo1Message.TOPIC, message);
    }

    public void asyncSend(Integer id, SendCallback callback){
        Demo1Message message = new Demo1Message();
        message.setId(id);
        rocketMQTemplate.asyncSend(Demo1Message.TOPIC, message, callback);
    }

    public void onewaySend(Integer id) {
        // 创建 Demo01Message 消息
        Demo1Message message = new Demo1Message();
        message.setId(id);
        // oneway 发送消息
        rocketMQTemplate.sendOneWay(Demo1Message.TOPIC, message);
    }

}
