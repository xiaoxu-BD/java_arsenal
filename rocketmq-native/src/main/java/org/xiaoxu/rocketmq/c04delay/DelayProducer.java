package org.xiaoxu.rocketmq.c04delay;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * c04-1 延迟消息（定时投递）
 *
 * 【运行顺序】先跑 {@link DelayConsumer} 挂着，再跑本类；观察"发出"与"收到"的时间差。
 *
 * 【知识点】
 * 1. 延迟消息：发出后先进 Broker 的内部延迟主题 SCHEDULE_TOPIC_XXXX 藏起来，
 *    到点后由定时服务搬运回原 Topic，消费者这时才能拉到——不是消费者端"等一会再处理"。
 * 2. 经典 API 是【固定级别】setDelayTimeLevel，共 18 级：
 *      1    2   3    4    5    6   7   8   9   10  11  12  13  14   15   16  17  18
 *      1s   5s  10s  30s  1m   2m  3m  4m  5m  6m  7m  8m  9m  10m  20m  30m 1h  2h
 *    只能从中选一个，不能"37 秒"这种任意值。
 * 3. 5.x 新玩法：msg.setDeliverTimeMs(毫秒时间戳) 支持任意时刻定时（秒级精度），
 *    但要求 Broker 开启 timer 功能（broker.conf 加 enableTimerScope=true 相关配置），
 *    本地/公司环境没开就用不了——所以先练 18 级，知道有这个进阶能力即可。
 * 4. 典型场景：下单 15 分钟未支付自动取消、定时任务、失败重试退避。
 *
 * 【预期输出】发送时打印"期望 10 秒后投递"，消费者那边的收到时间差不多正好 +10s。
 */
public class DelayProducer {

    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_DELAY_PRODUCER);
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);

        try {
            producer.start();
            System.out.println("生产者启动，发送延迟消息...（level 1=1s，level 3=10s，level 4=30s）");

            int[] levels = {1, 3, 4};
            for (int level : levels) {
                Message msg = new Message(
                        MqConst.TOPIC_DELAY,
                        "TagD",
                        "delay-key-L" + level,
                        ("延迟消息 level=" + level).getBytes(StandardCharsets.UTF_8));

                // 核心一行：设置延迟级别（对照上面 18 级表）
                msg.setDelayTimeLevel(level);

                SendResult result = producer.send(msg);
                System.out.printf("[%s] 发送成功 msgId=%s level=%d（1s/10s/30s 后投递）%n",
                        LocalTime.now().format(HH_MM_SS), result.getMsgId(), level);
            }
        } finally {
            producer.shutdown();
        }
    }
}
