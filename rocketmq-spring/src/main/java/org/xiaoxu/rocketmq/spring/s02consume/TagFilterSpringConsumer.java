package org.xiaoxu.rocketmq.spring.s02consume;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * s02-2 Tag 过滤消费者（对照 c07 TagFilterDemo）
 *
 * selectorExpression = 原生 subscribe(topic, "TagA || TagB") 的第二参数。
 * 服务端过滤、跳过提交位点、订阅关系必须一致——c07 吃透的机制原封不动，只是写法变了。
 *
 * 访问 http://localhost:8081/send/tag 发 9 条（TagA/TagB/TagC 各 3），
 * 这里只会收到 TagA/TagB 的 6 条。
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SpringTopic_Tag",
        consumerGroup = "spring-tag-consumer-group",
        selectorExpression = "TagA || TagB"   // 只订 TagA/TagB，TagC 服务端就滤掉了
)
public class TagFilterSpringConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("[Tag过滤 消费] {}", message);
    }
}
