package org.xiaoxu.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.xiaoxu.config.VerificationCodeMQConfig;
import org.xiaoxu.domain.dto.VerificationCodeMessage;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeDeadLetterConsumer {

    private final JavaMailSender mailSender;


    @RabbitListener(queues = VerificationCodeMQConfig.DEAD_QUEUE)
    public void handleDeadLetter(VerificationCodeMessage message, Channel channel, Message msg) throws IOException {
        try {
            log.error("【死信队列】验证码发送降级处理: phone={}, status={}, reason={}",
                    message.getPhone(), message.getStatus(), message.getMessage());

            // 发送告警邮件
            sendAlertMail(message);

            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("【死信队列】降级处理完成, phone={}", message.getPhone());
        } catch (Exception e) {
            log.error("【死信队列】降级处理异常, phone={}", message.getPhone(), e);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private void sendAlertMail(VerificationCodeMessage message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("tingqq7z@163.com");
            mail.setTo("tingqq7@gmail.com");
            mail.setSubject("【告警】验证码发送失败");
            mail.setText(String.format(
                    "验证码发送失败，详情如下：\n\n" +
                    "手机号: %s\n" +
                    "状态: %s\n" +
                    "原因: %s\n" +
                    "时间: %s\n",
                    message.getPhone(),
                    message.getStatus(),
                    message.getMessage(),
                    message.getTimestamp()
            ));
            mailSender.send(mail);
            log.info("【邮件】告警邮件发送成功, phone={}", message.getPhone());
        } catch (Exception e) {
            log.error("【邮件】告警邮件发送失败, phone={}", message.getPhone(), e);
        }
    }
}
