package org.xiaoxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.domain.channel.ChannelResult;
import org.xiaoxu.domain.channel.PayChannelFactory;
import org.xiaoxu.domain.dto.CallbackPayload;
import org.xiaoxu.domain.dto.PaymentRequest;
import org.xiaoxu.domain.dto.PaymentResponse;
import org.xiaoxu.domain.entity.PaymentOrder;
import org.xiaoxu.enums.PaymentStatus;
import org.xiaoxu.exception.BizException;
import org.xiaoxu.mapper.PaymentOrderMapper;
import org.xiaoxu.service.PayChannel;
import org.xiaoxu.service.PaymentService;

import java.time.LocalDateTime;

// service/PaymentServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderMapper orderMapper;
    private final PayChannelFactory channelFactory;

    // ==================== 1. 发起支付 ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse pay(PaymentRequest req) {

        // ---- 幂等校验 ----
        PaymentOrder existing = orderMapper.selectOne(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOrderId, req.getOrderId())
        );

        if (existing != null) {
            if (existing.getStatus() == PaymentStatus.SUCCESS) {
                throw new BizException("订单已支付，不可重复操作");
            }
            if (existing.getStatus() == PaymentStatus.PROCESSING) {
                throw new BizException("订单处理中，请勿重复提交");
            }
        }

        // ---- 插入支付单 ----
        PaymentOrder order = PaymentOrder.builder()
                .orderId(req.getOrderId())
                .userId(req.getUserId())
                .amount(req.getAmount())
                .payMethod(req.getPayMethod())
                .description(req.getDescription())
                .status(PaymentStatus.PROCESSING)
                .build();
        orderMapper.insert(order);

        log.info("支付单已创建 orderId={}, 开始调用渠道...", req.getOrderId());

        // ---- 调用渠道 ----
        try {
            PayChannel channel = channelFactory.getChannel(req.getPayMethod());
            ChannelResult result = channel.pay(order);

            if (result.isSuccess()) {
                // 成功
                orderMapper.update(null, new LambdaUpdateWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderId, req.getOrderId())
                        .set(PaymentOrder::getStatus, PaymentStatus.SUCCESS)
                        .set(PaymentOrder::getTransactionId, result.getTransactionId())
                        .set(PaymentOrder::getPayTime, LocalDateTime.now())
                );
                log.info("订单 {} 支付成功!", req.getOrderId());
                return PaymentResponse.success(req.getOrderId(), result.getTransactionId());
            } else {
                // 失败
                orderMapper.update(null, new LambdaUpdateWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderId, req.getOrderId())
                        .set(PaymentOrder::getStatus, PaymentStatus.FAILED)
                        .set(PaymentOrder::getFailReason, result.getErrorMessage())
                );
                return PaymentResponse.fail(req.getOrderId(), result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("支付异常 orderId={}", req.getOrderId(), e);
            orderMapper.update(null, new LambdaUpdateWrapper<PaymentOrder>()
                    .eq(PaymentOrder::getOrderId, req.getOrderId())
                    .set(PaymentOrder::getStatus, PaymentStatus.FAILED)
                    .set(PaymentOrder::getFailReason, e.getMessage())
            );
            return PaymentResponse.fail(req.getOrderId(), "系统异常，请稍后重试");
        }
    }

    // ==================== 2. 异步回调 ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(CallbackPayload payload) {

        // 验签
        if (payload.getSign() == null || payload.getSign().isBlank()) {
            throw new BizException("回调验签失败");
        }

        PaymentOrder order = orderMapper.selectOne(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOrderId, payload.getOrderId())
        );
        if (order == null) {
            throw new BizException("支付单不存在");
        }

        // 幂等
        if (order.getStatus() == PaymentStatus.SUCCESS) {
            log.info("回调重复, 忽略. orderId={}", order.getOrderId());
            return;
        }

        // 根据渠道状态更新
        switch (payload.getTradeStatus()) {
            case "TRADE_SUCCESS":
                orderMapper.update(null, new LambdaUpdateWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderId, payload.getOrderId())
                        .set(PaymentOrder::getStatus, PaymentStatus.SUCCESS)
                        .set(PaymentOrder::getTransactionId, payload.getTransactionId())
                        .set(PaymentOrder::getPayTime, LocalDateTime.now())
                );
                log.info("回调: 订单 {} 支付成功", payload.getOrderId());
                break;

            case "TRADE_CLOSED":
                orderMapper.update(null, new LambdaUpdateWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderId, payload.getOrderId())
                        .set(PaymentOrder::getStatus, PaymentStatus.CLOSED)
                );
                log.info("回调: 订单 {} 已关闭", payload.getOrderId());
                break;

            default:
                log.warn("回调: 未知状态 {}", payload.getTradeStatus());
        }
    }

    // ==================== 3. 查询状态 ====================
    @Override
    public PaymentResponse queryStatus(String orderId) {

        PaymentOrder order = orderMapper.selectOne(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOrderId, orderId)
        );
        if (order == null) {
            throw new BizException("支付单不存在: " + orderId);
        }

        PaymentResponse resp = new PaymentResponse();
        resp.setOrderId(order.getOrderId());
        resp.setStatus(order.getStatus().name());
        resp.setTransactionId(order.getTransactionId());
        resp.setPayTime(order.getPayTime());
        resp.setMessage(order.getFailReason() != null ? order.getFailReason() : "查询成功");
        return resp;
    }
}