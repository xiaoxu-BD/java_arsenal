package org.xiaoxu.wecommail.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.payment.dto.CreateOrderRequest;
import org.xiaoxu.wecommail.payment.dto.EntitlementResponse;
import org.xiaoxu.wecommail.payment.dto.PayUrlResponse;
import org.xiaoxu.wecommail.payment.entity.PaymentOrder;
import org.xiaoxu.wecommail.payment.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/orders")
    public PaymentOrder create(@RequestBody CreateOrderRequest request) {
        return payments.createOrder(request.requestId(), request.productCode());
    }

    @GetMapping("/orders/{orderNo}")
    public PaymentOrder get(@PathVariable String orderNo) {
        return payments.getOrder(orderNo);
    }

    @PostMapping("/orders/{orderNo}/pay")
    public PayUrlResponse pay(@PathVariable String orderNo) {
        return new PayUrlResponse(payments.startPayment(orderNo));
    }

    @PostMapping("/orders/{orderNo}/sync")
    public PaymentOrder sync(@PathVariable String orderNo) {
        return payments.syncOrder(orderNo);
    }

    @GetMapping("/entitlement")
    public EntitlementResponse entitlement() {
        return new EntitlementResponse(payments.getServiceDays());
    }

    @PostMapping(value = "/alipay/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String notify(@RequestParam Map<String, String> parameters) {
        try {
            // Spring 事务代理返回后才应答 success，提交失败时渠道应继续重试。
            payments.handleNotification(parameters);
            return "success";
        } catch (RuntimeException e) {
            log.warn("支付通知处理失败，orderNo={}", parameters.get("out_trade_no"), e);
            return "failure";
        }
    }
}
