package org.xiaoxu;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xiaoxu.producer.Demo01Producer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @className: Demo01ProducerTest
 * @author: xiaoxu
 * @date: 2025/10/4 22:21
 * @Version: 1.0
 * @description:
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RocketMqApplication.class)
public class Demo01ProducerTest {

    private static final Logger log = LoggerFactory.getLogger(Demo01ProducerTest.class);
    @Autowired
    private Demo01Producer producer;


    @Test
    public void testSyncSend() {
        int id = (int) (System.currentTimeMillis() / 1000);
        SendResult result = producer.syncSend(id);
        log.info("[testSyncSend][发送编号：[{}] 发送结果：[{}]]", id, result);

        // 增加断言，验证发送状态是成功的，这明确地证明了连接和发送操作都已成功。
        Assert.notNull(result, "发送结果不应为 null");
        Assert.isTrue(result.getSendStatus() == SendStatus.SEND_OK, "消息发送失败");
    }

    @Test
    public void testASyncSend() throws InterruptedException {
        int id = (int) (System.currentTimeMillis() / 1000);
        CountDownLatch latch = new CountDownLatch(1);
        // 使用 Lambda 表达式简化回调
        producer.asyncSend(id, new SendCallback() { // Using a lambda is also a great option here
           @Override
           public void onSuccess(SendResult result) {
               log.info("[testASyncSend][发送编号：[{}] 发送成功，结果为：[{}]]", id, result);
               latch.countDown();
           }
           @Override
           public void onException(Throwable e) {
               // 异常情况建议使用 log.error
               log.error("[testASyncSend][发送编号：[{}] 发送异常]]", id, e);
               latch.countDown();
           }
        });

        // 阻塞等待，保证消费
        latch.await(10, TimeUnit.SECONDS);
    }


    @Test
    public void testOnewaySend() throws InterruptedException {
        int id = (int) (System.currentTimeMillis() / 1000);
        producer.onewaySend(id);
        log.info("[testOnewaySend][发送编号：[{}] 发送完成]", id);

        // oneway发送无法保证消息一定发送成功，这里稍微等待一下，以便观察broker的日志
        Thread.sleep(1000);
    }

}
