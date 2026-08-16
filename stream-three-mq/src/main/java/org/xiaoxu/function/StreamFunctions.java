package org.xiaoxu.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Spring Cloud Stream 函数式编程模型：业务代码只是普通函数 bean，
 * 绑定到哪个 MQ 完全由 application.yml 的 bindings 决定——这就是"屏蔽差异"。
 *
 * 绑定命名规则：bean 名 + "-in-0" / "-out-0"
 */
@Configuration
public class StreamFunctions {

    private static final Logger log = LoggerFactory.getLogger(StreamFunctions.class);

    /**
     * 消费 RocketMQ（order-in-0 → stream-order-topic）。
     * 用 Message<String> 而非裸 String，为了把 binder 注入的 headers 打出来看个究竟。
     */
    @Bean
    public Consumer<Message<String>> order() {
        return msg -> log.info("[RocketMQ 消费] body={} headers={}", msg.getPayload(), msg.getHeaders());
    }

    /**
     * 消费 RabbitMQ（mail-in-0 → stream-mail exchange）。
     * 与 order() 代码风格完全相同——区别只在 yml 里的 binder 字段。
     */
    @Bean
    public Consumer<Message<String>> mail() {
        return msg -> log.info("[RabbitMQ 消费] body={} headers={}", msg.getPayload(), msg.getHeaders());
    }

    /**
     * 跨 binder 处理器：RocketMQ 进（stream-notify-topic）→ 加工 → RabbitMQ 出（stream-notify-result）。
     * Function 的返回值自动发到 *-out-0 绑定的目的地，无需手动 send。
     */
    @Bean
    public Function<String, String> notifyProcess() {
        return body -> {
            String result = "[processed@" + System.currentTimeMillis() + "] " + body;
            log.info("[跨binder处理] 收自 RocketMQ: {} → 将发往 RabbitMQ: {}", body, result);
            return result;
        };
    }

    /**
     * 消费跨 binder 的最终结果（stream-notify-result exchange）。
     * 没有这个带 group 的消费者，Rabbit 侧不会声明队列，函数的输出会被 exchange 静默丢弃。
     */
    @Bean
    public Consumer<Message<String>> notifyResult() {
        return msg -> log.info("[跨binder终点] RabbitMQ 收到最终结果: {}", msg.getPayload());
    }
}
