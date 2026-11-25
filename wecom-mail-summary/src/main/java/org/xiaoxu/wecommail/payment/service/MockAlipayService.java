package org.xiaoxu.wecommail.payment.service;

import org.xiaoxu.wecommail.payment.entity.MockAlipayTrade;
import org.xiaoxu.wecommail.payment.entity.PaymentOrder;
import org.xiaoxu.wecommail.payment.mapper.MockAlipayTradeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 模拟支付宝渠道：独立维护渠道交易表，用方法调用模拟下单/查单 RPC，
 * 通知由异步任务按真实支付宝的形式（RSA2 签名表单 HTTP POST）推给商户。
 */
@Service
public class MockAlipayService {

    public static final String APP_ID = "demo-mail-app";
    public static final String SELLER_ID = "demo-mail-seller";

    private final MockAlipayTradeMapper trades;
    private final MockAlipaySignature signature;

    public MockAlipayService(MockAlipayTradeMapper trades, MockAlipaySignature signature) {
        this.trades = trades;
        this.signature = signature;
    }

    // 用方法调用模拟下单 RPC，渠道不读取或修改商户订单表。e
    public void createTrade(PaymentOrder order) {
        MockAlipayTrade existing = trades.selectByOrderNo(order.getOrderNo());
        if (existing != null) {
            return;
        }
        MockAlipayTrade trade = new MockAlipayTrade();
        trade.setOrderNo(order.getOrderNo());
        trade.setTradeNo("ALI" + UUID.randomUUID().toString().replace("-", ""));
        trade.setAmount(order.getAmount());
        trade.setExpiresAt(order.getExpiresAt());
        trades.insert(trade);
    }

    @Transactional
    public MockAlipayTrade query(String orderNo) {
        MockAlipayTrade trade = trades.selectByOrderNoForUpdate(orderNo);
        if (trade == null) {
            return null;
        }
        closeIfExpired(trade);
        return trades.selectByOrderNo(orderNo);
    }

    @Transactional
    public MockAlipayTrade pay(String orderNo) {
        MockAlipayTrade trade = trades.selectByOrderNoForUpdate(orderNo);
        if (trade == null) {
            throw new IllegalArgumentException("渠道订单不存在，请先发起支付");
        }
        // 关单和支付竞争同一行锁，超时后不能再把渠道订单改成成功。
        closeIfExpired(trade);
        trade = trades.selectByOrderNo(orderNo);
        if (MockAlipayTrade.STATUS_WAIT_BUYER_PAY.equals(trade.getStatus())) {
            trades.updatePaySuccess(orderNo, LocalDateTime.now());
        }
        return trades.selectByOrderNo(orderNo);
    }

    public List<MockAlipayTrade> pendingNotifications() {
        return trades.selectPendingNotifications(100);
    }

    public Map<String, String> notification(MockAlipayTrade trade) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("app_id", APP_ID);
        parameters.put("seller_id", SELLER_ID);
        parameters.put("out_trade_no", trade.getOrderNo());
        parameters.put("trade_no", trade.getTradeNo());
        parameters.put("total_amount", trade.getAmount().toPlainString());
        parameters.put("trade_status", trade.getStatus());
        if (trade.getPaidAt() != null) {
            parameters.put("gmt_payment", trade.getPaidAt().toString());
        }
        parameters.put("sign_type", "RSA2");
        parameters.put("sign", signature.sign(parameters));
        return parameters;
    }

    public void acknowledge(String orderNo) {
        trades.updateNotifyAck(orderNo);
    }

    private void closeIfExpired(MockAlipayTrade trade) {
        if (MockAlipayTrade.STATUS_WAIT_BUYER_PAY.equals(trade.getStatus())
                && !LocalDateTime.now().isBefore(trade.getExpiresAt())) {
            trades.updateClose(trade.getOrderNo());
        }
    }
}
