package org.xiaoxu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.service.PayService;

/**
 * @className: StrategyController
 * @author: xiaoxu
 * @date: 2025/8/7 8:05
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/strategy")
public class StrategyController {

    @Autowired
    private PayService payService;


    @GetMapping("pay")
    public String pay(@RequestParam("type") String type, @RequestParam("amount") double amount) {
        payService.pay(type, amount);
        return "支付成功";
    }
}
