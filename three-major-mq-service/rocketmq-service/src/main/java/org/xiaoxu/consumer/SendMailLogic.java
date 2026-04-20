package org.xiaoxu.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @className: SendMailLogic
 * @author: xiaoxu
 * @date: 2026/4/19 9:21
 * @Version: 1.0
 * @description:
 */
@Component
@RocketMQMessageListener(topic = "rocket-mail-topic", consumerGroup = "email-consumer-group",selectorExpression = "mail")
public class SendMailLogic implements RocketMQListener<Map<String,String>> {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Override
    public void onMessage(Map<String, String> message) {
        String targetEmail = message.get("email");
        String content = message.get("content");
        String sendTimestamp =message.get("sendTimestamp");
        long receiveTimestamp = System.currentTimeMillis();
        long startTime = Long.parseLong(sendTimestamp);
        long delay = receiveTimestamp - startTime;


        long businessStart = System.currentTimeMillis();

        try {
            sendToEmail(targetEmail, content);
            long businessEnd = System.currentTimeMillis();
            long executionTime = businessEnd - businessStart;

            // 打印统计结果
            System.out.println("------------------------------------");
            System.out.println("【链路监控】消息在 MQ 中排队/传输耗时：" + delay + "ms");
            System.out.println("【业务监控】邮件发送逻辑执行耗时：" + executionTime + "ms");
            System.out.println("【总计】用户感知到的总延迟：" + (receiveTimestamp - Long.parseLong(sendTimestamp) + executionTime) + "ms");
            System.out.println("------------------------------------");
            System.out.println("邮件已发送");
        } catch (MailException e) {
            System.out.println("邮件发送失败:原因 " + e.getMessage());
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


}
