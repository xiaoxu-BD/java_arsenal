package org.xiaoxu.wecommail.payment.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.xiaoxu.wecommail.payment.entity.MockAlipayTrade;
import org.xiaoxu.wecommail.payment.entity.PaymentOrder;
import org.xiaoxu.wecommail.payment.mapper.PaymentOrderMapper;
import org.xiaoxu.wecommail.payment.service.MockAlipayService;
import org.xiaoxu.wecommail.payment.service.PaymentService;

/**
 * 支付后台任务：渠道异步通知（真实支付宝为服务器到服务器的 HTTP 推送）与主动查单对账。
 */
@Component
public class PaymentJobs {

    private static final Logger log = LoggerFactory.getLogger(PaymentJobs.class);
    private final MockAlipayService alipay;
    private final PaymentService payments;
    private final PaymentOrderMapper orders;
    private final String notifyUrl;
    private final boolean reconcileEnabled;
    private final RestClient client;

    public PaymentJobs(MockAlipayService alipay, PaymentService payments, PaymentOrderMapper orders,
                       @Value("${payment.notify-url}") String notifyUrl,
                       @Value("${payment.reconcile-enabled:true}") boolean reconcileEnabled) {
        this.alipay = alipay;
        this.payments = payments;
        this.orders = orders;
        this.notifyUrl = notifyUrl;
        this.reconcileEnabled = reconcileEnabled;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(2000);
        client = RestClient.builder().requestFactory(factory).build();
    }

    @Scheduled(fixedDelayString = "${payment.notify-delay-ms:5000}", initialDelay = 5000)
    public void deliverNotifications() {
        log.info("每5s自动回调");
        for (MockAlipayTrade trade : alipay.pendingNotifications()) {
            try {
                LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                alipay.notification(trade).forEach(form::add);
                String result = client.post().uri(notifyUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form).retrieve().body(String.class);
                if ("success".equals(result)) {
                    alipay.acknowledge(trade.getOrderNo());
                } else {
                    log.warn("商户未确认支付通知，下轮重试，orderNo={}", trade.getOrderNo());
                }
            } catch (RuntimeException e) {
                log.warn("支付通知发送失败，下轮重试，orderNo={}", trade.getOrderNo(), e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${payment.reconcile-delay-ms:30000}", initialDelay = 30000)
    public void reconcileOrders() {
        if (!reconcileEnabled) {
            return;
        }
        for (PaymentOrder order : orders.selectWaitingOrders(100)) {
            try {
                log.info("定时任务执行查询订单状态 查询之前的状态： {}",order.getStatus());
                payments.syncOrder(order.getOrderNo());
            } catch (RuntimeException e) {
                log.warn("支付查单失败，orderNo={}", order.getOrderNo(), e);
            }
        }
    }
}
