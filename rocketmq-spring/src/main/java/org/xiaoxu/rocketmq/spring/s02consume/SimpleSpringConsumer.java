package org.xiaoxu.rocketmq.spring.s02consume;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * s02-1 最简消费者（对照 c01 SimpleConsumer）
 *
 * 【原生映射】一个注解 = 原生七八行：
 *   @RocketMQMessageListener(topic, consumerGroup)
 *     = new DefaultMQPushConsumer(group)
 *       + setNamesrvAddr(全局配置)
 *       + subscribe(topic, "*")
 *       + registerMessageListener(...)
 *       + start()
 *   容器关闭时自动 shutdown —— c01 里"start 后进程就退了"、"位点没提交就退出了"
 *   这些生命周期坑，starter 全部替你管了。
 *
 * 【返回值 API 的变化——重要！】
 *   原生：consumeMessage 返回 CONSUME_SUCCESS / RECONSUME_LATER
 *   starter：RocketMQListener.onMessage() 是 void——
 *     正常返回 = CONSUME_SUCCESS
 *     抛异常   = RECONSUME_LATER（自动进重试，对照 c08 的三部曲）
 *   所以 c08 学的"业务失败要么抛异常要么主动申报"，注解版统一成：抛就完事了。
 *
 * 【泛型 = 消息体类型】RocketMQListener<String>：payload 是 String 直接给你；
 *   写成实体类（如 OrderDTO）则自动 JSON 反序列化——不用再 new String(body, UTF_8) 手工解码。
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SpringTopic_Sync",              // = subscribe("SpringTopic_Sync", ...)
        consumerGroup = "spring-sync-consumer-group",  // = new DefaultMQPushConsumer(group)
        selectorExpression = "*",                // = subscribe(topic, "*") 第二参数
        consumeMode = ConsumeMode.CONCURRENTLY,  // = MessageListenerConcurrently（并发）
        messageModel = MessageModel.CLUSTERING   // = setMessageModel(CLUSTERING) 默认集群
)
public class SimpleSpringConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        // 正常走完 = CONSUME_SUCCESS；这里抛任何异常 = RECONSUME_LATER 自动重试
        log.info("[Simple 消费] {}", message);
    }
}
