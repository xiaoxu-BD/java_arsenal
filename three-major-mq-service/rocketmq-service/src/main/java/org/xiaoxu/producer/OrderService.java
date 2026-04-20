package org.xiaoxu.producer;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

   private static final  Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void createOrder(String userEmail, String orderId) {
        // 1. 模拟处理业务逻辑
        logger.info("订单已创建：{}", orderId);

        // 2. 构建消息体（建议用 JSON 字符串）
        Map<String, String> msgBody = new HashMap<>();
        msgBody.put("email", userEmail);
        msgBody.put("content", "time is "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +"\n" + "您的订单 " + orderId + " 已下单成功!" + "\n" + "以上内容来自自动化接口测试~");
        msgBody.put("sendTimestamp", String.valueOf(System.currentTimeMillis()));
        // 3. 发送异步消息，不阻塞主流程
        rocketMQTemplate.convertAndSend("rocket-mail-topic:mail", msgBody);
        
        System.out.println("消息已送往 RocketMQ，主流程继续执行...");
    }
}