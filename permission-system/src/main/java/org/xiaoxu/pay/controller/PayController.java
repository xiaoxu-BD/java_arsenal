package org.xiaoxu.pay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.pay.dto.CancelOrderRequest;
import org.xiaoxu.pay.dto.CreateOrderRequest;
import org.xiaoxu.pay.entity.PayOrder;
import org.xiaoxu.pay.service.PayService;
import org.xiaoxu.pay.vo.PayResultVO;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 创建订单（测试用）
     */
    @PostMapping("/create")
    public Result<PayOrder> createOrder(@RequestBody @Valid CreateOrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        PayOrder order = payService.createPayOrder(
                request.getBusinessType(),
                request.getBusinessId(),
                null,
                userId,
                username,
                request.getProductName(),
                request.getAmount()
        );
        return Result.success(order);
    }

    /**
     * 测试支付（GET，无需登录，直接跳转支付宝）
     */
    @GetMapping("/test/{orderNo}")
    public String testPay(@PathVariable String orderNo) {
        PayResultVO result = payService.pay(orderNo, 1L);
        return result.getPayForm();
    }

    /**
     * 发起支付
     */
    @PostMapping("/pay/{orderNo}")
    public Result<PayResultVO> pay(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PayResultVO result = payService.pay(orderNo, userId);
        return Result.success(result);
    }

    /**
     * 支付宝异步回调
     */
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
            }
            params.put(name, valueStr.toString());
        }

        log.info("收到支付回调: {}", params);
        return payService.handleNotify(params);
    }

    /**
     * 查询我的付款单列表
     */
    @GetMapping("/my")
    public Result<IPage<PayOrder>> myOrders(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(payService.queryUserOrders(userId, status, current, size));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/status/{orderNo}")
    public Result<PayOrder> queryStatus(@PathVariable String orderNo) {
        PayOrder order = payService.queryOrder(orderNo);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody @Valid CancelOrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        payService.cancelOrder(request.getOrderNo(), userId, request.getReason());
        return Result.success();
    }
}
