package org.xiaoxu.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @className: SendMailLogicConsumer
 * @author: xiaoxu
 * @date: 2026/4/19 9:21
 * @Version: 1.0
 * @description:
 */
@Component
@RocketMQMessageListener(topic = "rocket-mail-topic", consumerGroup = "email-consumer-group",selectorExpression = "mail")
public class SendMailLogicConsumer implements RocketMQListener<Map<String,String>> {

    private static final Logger log = LoggerFactory.getLogger(SendMailLogicConsumer.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void onMessage(Map<String, String> message) {
        String targetEmail = message.get("email");
        String content = message.get("content");
        String sendTimestamp = message.get("sendTimestamp");

        // 手动注入/上游异常都可能缺字段：裸消息记日志后直接 ACK，不拖进重试循环
        if (targetEmail == null || targetEmail.isEmpty()) {
            log.warn("[邮件] 消息缺 email 字段，跳过发送（生产上应入库+告警）: {}", message);
            return;
        }

        long receiveTimestamp = System.currentTimeMillis();
        long startTime = parseSafely(sendTimestamp);
        long delay = startTime > 0 ? receiveTimestamp - startTime : -1;

        long businessStart = System.currentTimeMillis();
        try {
            sendToEmail(targetEmail, content != null ? content : "");
            long executionTime = System.currentTimeMillis() - businessStart;
            log.info("[链路监控] MQ 排队/传输耗时: {}ms, 邮件发送耗时: {}ms, 总延迟: {}ms{}",
                    delay,
                    executionTime,
                    delay >= 0 ? delay + executionTime : -1,
                    delay < 0 ? "（无 sendTimestamp，跳过链路耗时统计）" : "");
        } catch (MailException e) {
            log.error("[邮件] 发送失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendToEmail(String targetEmail, String content) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(targetEmail);
        mail.setSubject("订单已创建");
        mail.setText(content);
        mailSender.send(mail);
    }

    private static long parseSafely(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
