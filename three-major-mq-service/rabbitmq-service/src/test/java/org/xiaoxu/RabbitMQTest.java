package org.xiaoxu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.product.ProductorMessage;

/**
 * @className: RabbitMQTest
 * @author: xiaoxu
 * @date: 2025/11/13 10:34
 * @Version: 1.0
 * @description:
 */
@SpringBootTest
public class RabbitMQTest {


    @Autowired
    private ProductorMessage productorMessage;



    @Test
    public void testSendMessage() throws InterruptedException{

        for (int i = 0; i < 10; i++) {
            String message = "hello mock register" + i;

            productorMessage.sendMessage(message);
        }


    }


    @Test
    public void testNoBinding(){
        System.out.println("开始测试没有绑定的交换机");
        productorMessage.sendNoBindingRouteMessage("hello mock register");
    }


    @Test
    public void testNoExchange(){
        System.out.println("开始测试没有交换机的消息");
        productorMessage.sendNoExchangeMessage("noExistExchange");
    }


    @Test
    public void testSendMessageToNormal(){
        System.out.println("开始测试发送消息到正常队列");
        productorMessage.sendMessageToNormal("this message is go to dead exchange");
    }
}
