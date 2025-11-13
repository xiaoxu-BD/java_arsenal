package org.xiaoxu.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.xiaoxu.config.RabbitMQConfig;

import java.io.IOException;

/**
 * @className: ConsumerMessage
 * @author: xiaoxu
 * @date: 2025/11/13 10:32
 * @Version: 1.0
 * @description:
 */
@Component
public class ConsumerMessage {


    private static final Logger log = LoggerFactory.getLogger(ConsumerMessage.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receive(Message message, Channel channel){
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageEntity = new String(message.getBody());
        log.info("消息id:{}",deliveryTag);
        log.info("消息实体: {} ",messageEntity);

        try {

            Thread.sleep(3000);
            int i = 1/0;
            log.info("消息消费成功");
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error("消息消费失败：{}",e.getMessage());
            try {
                //不重新入队
                channel.basicNack(deliveryTag,false,false);
            } catch (Exception ex) {
                log.error("消息消费失败：{}",ex.getMessage());
            }
        }
    }



    @RabbitListener(queues = RabbitMQConfig.NORMAL_QUEUE)
    public void  receiveWithNormal(Message message,Channel channel){
        String msg = new String(message.getBody());
        System.out.println("收到消息: " + msg);

        try {
            // 模拟处理逻辑
            if (msg.contains("fail")) {
                // 拒绝消息，不重入队列 -> 触发死信
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                System.out.println("消息被拒绝，进入死信队列: " + msg);
            } else {
                // 正常消费
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                System.out.println("消息消费成功: " + msg);
            }
        } catch (Exception e) {
            // 出现异常也可以 nack
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    @RabbitListener(queues = RabbitMQConfig.DEAD_QUEUE)
    public void receiveDead(Message message, Channel channel) {
        String msg = new String(message.getBody());
        System.out.println("收到死信消息: " + msg);
     }
}
