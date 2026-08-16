package org.xiaoxu.producer;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * @className: ProduceRockerMQDEM
 * @author: xiaoxu
 * @date: 2026/4/18 23:25
 * @Version: 1.0
 * @description: 同步/异步两种发送方式的对照
 */
@Service
public class ProduceRockerMQDEM {

    private static final Logger logger = LoggerFactory.getLogger(ProduceRockerMQDEM.class);
    private static final String TOPIC = "rocket-service-exm";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送：调用线程阻塞到 broker 确认才返回
     */
    public void send(String msg) {
        Message<String> message = MessageBuilder.withPayload(msg)
                .setHeader(RocketMQHeaders.KEYS, "ORDER-1001")
                .build();
        long start = System.currentTimeMillis();
        rocketMQTemplate.syncSend(TOPIC, message);
        logger.info("[同步发送] 线程 {} 阻塞 {}ms 拿到 broker 确认才返回，业务键=ORDER-1001",
                Thread.currentThread().getName(), System.currentTimeMillis() - start);
    }

    /**
     * 发一条注定失败的消息：body 以 poison 开头，消费者会抛异常，走 重试->死信 全链路
     */
    public void sendPoison() {
        Message<String> message = MessageBuilder.withPayload("poison-" + System.currentTimeMillis())
                .setHeader(RocketMQHeaders.KEYS, "POISON-2001")
                .build();
        rocketMQTemplate.syncSend(TOPIC, message);
        logger.info("[毒消息] 已发出，业务键=POISON-2001，坐等 重试->死信 全链路");
    }

    /**
     * 异步发送：立即返回，broker 的确认结果由回调线程送达
     */
    public void sendAsync(String msg) {
        Message<String> message = MessageBuilder.withPayload(msg)
                .setHeader(RocketMQHeaders.KEYS, "ORDER-1002")
                .build();
        logger.info("[异步发送] 线程 {} 发起 asyncSend，主流程不等待确认，业务键=ORDER-1002",
                Thread.currentThread().getName());
        rocketMQTemplate.asyncSend(TOPIC, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                logger.info("[异步回调-成功] 线程 {} msgId={} queue={} status={}",
                        Thread.currentThread().getName(), result.getMsgId(),
                        result.getMessageQueue().getQueueId(), result.getSendStatus());
            }

            @Override
            public void onException(Throwable e) {
                logger.error("[异步回调-失败] 线程 {} 发送失败，生产上这里必须落补偿表或告警",
                        Thread.currentThread().getName(), e);
            }
        });
    }
}
