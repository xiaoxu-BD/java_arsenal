package org.xiaoxu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xiaoxu.config.VerificationCodeMQConfig;
import org.xiaoxu.domain.dto.VerificationCodeMessage;
import org.xiaoxu.service.SendCodeService;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendCodeServiceImpl implements SendCodeService {

    private final RabbitTemplate rabbitTemplate;

    @Async
    @Override
    public void asyncSendCode(String phone) {
        log.info("开始向第三方厂商请求发送验证码, phone={}", phone);

        VerificationCodeMessage message;
        try {
            // 模拟厂商接口耗时 ~5s
            Thread.sleep(5000);

            // 模拟生成4位验证码
            String code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            log.info("厂商返回验证码, phone={}, code={}", phone, code);
//            throw new InterruptedException();
            message = new VerificationCodeMessage(phone, code, "SUCCESS", "验证码发送成功", LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            message = new VerificationCodeMessage(phone, null, "FAILED", "厂商接口调用异常: " + e.getMessage(), LocalDateTime.now());
            log.error("调用厂商接口异常, phone={}", phone, e);
        }


        // 厂商返回结果后，发送MQ通知
        rabbitTemplate.convertAndSend(
                VerificationCodeMQConfig.EXCHANGE,
                VerificationCodeMQConfig.ROUTING_KEY,
                message
        );
        log.info("验证码结果已发送至MQ, phone={}, status={}", phone, message.getStatus());
    }
}
