package org.xiaoxu.pay.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 支付成功重定向 —— 将支付宝 returnUrl 重定向到前端 SPA
 */
@Controller
public class PayRedirectController {

    @Value("${alipay.frontendUrl:http://localhost:5173/pay/success}")
    private String frontendUrl;

    @GetMapping("/pay/success")
    public String paySuccess(HttpServletRequest request) {
        String outTradeNo = request.getParameter("out_trade_no");
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                .queryParam("out_trade_no", outTradeNo)
                .toUriString();
        return "redirect:" + redirectUrl;
    }
}
