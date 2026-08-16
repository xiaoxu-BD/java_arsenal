package org.xiaoxu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.domain.dto.CallbackPayload;
import org.xiaoxu.domain.dto.PaymentRequest;
import org.xiaoxu.domain.dto.PaymentResponse;
import org.xiaoxu.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /** 发起支付 */
    @PostMapping("/pay")
    public PaymentResponse pay(@RequestBody @Valid PaymentRequest request) {
        return paymentService.pay(request);
    }

    /** 第三方异步回调 */
    @PostMapping("/callback")
    public String callback(@RequestBody CallbackPayload payload) {
        paymentService.handleCallback(payload);
        return "success"; // 通知渠道方已收到
    }

    /** 查询支付状态 */
    @GetMapping("/query/{orderId}")
    public PaymentResponse query(@PathVariable String orderId) {
        return paymentService.queryStatus(orderId);
    }
}