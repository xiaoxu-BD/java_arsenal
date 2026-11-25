package org.xiaoxu.wecommail.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.payment.entity.MockAlipayTrade;
import org.xiaoxu.wecommail.payment.service.MockAlipayService;

/**
 * 模拟支付宝侧接口：对应真实场景里的支付宝收银台和钱包 App，仅供演示。
 */
@RestController
@RequestMapping("/mock/alipay/trades")
public class MockAlipayController {

    private final MockAlipayService alipay;

    public MockAlipayController(MockAlipayService alipay) {
        this.alipay = alipay;
    }

    @GetMapping("/{orderNo}")
    public MockAlipayTrade get(@PathVariable String orderNo) {
        MockAlipayTrade trade = alipay.query(orderNo);
        if (trade == null) {
            throw new IllegalArgumentException("渠道订单不存在");
        }
        return trade;
    }

    @PostMapping("/{orderNo}/pay")
    public MockAlipayTrade pay(@PathVariable String orderNo) {
        return alipay.pay(orderNo);
    }
}
