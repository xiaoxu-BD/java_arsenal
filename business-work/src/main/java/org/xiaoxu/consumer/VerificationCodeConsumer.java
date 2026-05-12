package org.xiaoxu.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.xiaoxu.config.VerificationCodeMQConfig;
import org.xiaoxu.domain.dto.VerificationCodeMessage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeConsumer {

    private static final String CODE_KEY_PREFIX = "verify:code:";
    private static final long CODE_EXPIRE_MINUTES = 5;

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = VerificationCodeMQConfig.QUEUE)
    public void handleVerificationCode(VerificationCodeMessage message, Channel channel, Message msg) throws IOException {
        try {
            log.info("收到验证码结果通知: phone={}, code={}, status={}",
                    message.getPhone(), message.getCode(), message.getStatus());

            if ("SUCCESS".equals(message.getStatus())) {
                // 缓存验证码到Redis，5分钟过期
                String key = CODE_KEY_PREFIX + message.getPhone();
                redisTemplate.opsForValue().set(key, message.getCode(), CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                log.info("验证码已缓存至Redis, key={}, expire={}min", key, CODE_EXPIRE_MINUTES);
                channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            } else {
                log.warn("验证码发送失败, 转入死信队列: phone={}, reason={}", message.getPhone(), message.getMessage());
                // requeue=false → 进入死信队列做降级处理
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            }
        } catch (Exception e) {
            log.error("处理验证码消息异常", e);
            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
