package org.xiaoxu.web_boot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @className: EmailUtil
 * @author: xiaoxu
 * @date: 2025/8/10 9:48
 * @Version: 1.0
 * @description:
 */
@Component
@RequiredArgsConstructor
public class EmailUtil {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;


    public void sendErrorMail(String to,String subject,String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        // 发送邮件
        javaMailSender.send(message);
    }

}
