package org.xiaoxu.producer;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

   private static final  Logger logger = LoggerFactory.getLogger(OrderService.class);


   private static final String EMAIL_DESTINATION = "rocket-mail-topic:mail";
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void createOrder(String userEmail, String orderId) {
        // 1. 模拟处理业务逻辑
        logger.info("订单已创建：{}", orderId);

        // 2. 构建消息体（建议用 JSON 字符串）
        Map<String, String> msgBody = new HashMap<>();
        msgBody.put("email", userEmail);
        msgBody.put("content", "time is "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +"\n" + "您的订单 " + orderId + " 已下单成功!" + "\n" + "以上内容来自自动化接口测试~");
        msgBody.put("sendTimestamp", String.valueOf(System.currentTimeMillis()));
        // 3. 发送同步消息，不阻塞主流程

        // —冒号前是 topic，冒号后是 tag
      /*  rocketMQTemplate.convertAndSend(EMAIL_DESTINATION, msgBody);*/

        Message<Map<String,String>> messageBody = MessageBuilder.withPayload(msgBody)
                                    .setHeader(RocketMQHeaders.KEYS,userEmail)
                                .build();
        rocketMQTemplate.asyncSend(EMAIL_DESTINATION, messageBody, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("消息发送成功， 发送成功{}",sendResult.toString());
            }

            @Override
            public void onException(Throwable e) {
            logger.error("发送失败 ,原因{}",e.getMessage());
            }
        });

        /**
         * 如果是异步消息
         */
/*        rocketMQTemplate.asyncSend(EMAIL_DESTINATION, msgBody, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("发送成功 msgId={} queue={}", result.getMsgId(), result.getMessageQueue());
            }

            @Override
            public void onException(Throwable e) {
                logger.error("发送失败，写补偿表或告警", e);  // 异步发送唯一能兜底的地方，千万别空着
            }
        });*/

        System.out.println("消息已送往 RocketMQ，主流程继续执行...");
    }
}