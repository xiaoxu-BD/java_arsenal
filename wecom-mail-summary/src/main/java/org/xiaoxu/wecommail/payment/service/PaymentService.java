package org.xiaoxu.wecommail.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.wecommail.payment.entity.MailServiceGrant;
import org.xiaoxu.wecommail.payment.entity.MockAlipayTrade;
import org.xiaoxu.wecommail.payment.entity.PaymentOrder;
import org.xiaoxu.wecommail.payment.mapper.MailServiceGrantMapper;
import org.xiaoxu.wecommail.payment.mapper.PaymentOrderMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // 项目还没有登录模块，演示时由服务端固定用户，不能让前端随意传 userId。
    public static final long DEMO_USER_ID = 1001L;

    private static final String PRODUCT_CODE = "MAIL_MONTH";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("19.90");
    private static final String PRODUCT_SUBJECT = "邮件汇总服务 30 天";
    private static final int SERVICE_DAYS = 30;
    private static final int ORDER_EXPIRE_MINUTES = 15;

    /** Redis 防重复提交窗口：高频重复由它挡下，长期重复由数据库 request_id 唯一约束兜底 */
    private static final Duration IDEMPOTENT_TTL = Duration.ofSeconds(30);
    /** 权益缓存 TTL：支付成功会主动删除，TTL 只兜极端情况下删除失败的一致性窗口 */
    private static final Duration ENTITLEMENT_TTL = Duration.ofMinutes(5);
    private static final String IDEMPOTENT_KEY = "payment:idem:";
    private static final String ENTITLEMENT_KEY = "payment:entitlement:";

    private final PaymentOrderMapper orders;
    private final MailServiceGrantMapper grants;
    private final MockAlipayService alipay;
    private final MockAlipaySignature signature;
    private final StringRedisTemplate redis;

    public PaymentService(PaymentOrderMapper orders, MailServiceGrantMapper grants, MockAlipayService alipay,
                          MockAlipaySignature signature, StringRedisTemplate redis) {
        this.orders = orders;
        this.grants = grants;
        this.alipay = alipay;
        this.signature = signature;
        this.redis = redis;
    }

    public PaymentOrder createOrder(String requestId, String productCode) {
        if (requestId == null || !requestId.matches("[a-zA-Z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("requestId 必须为 1~64 位字母、数字、下划线或连字符");
        }
        if (!PRODUCT_CODE.equals(productCode)) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (!tryMarkRequest(requestId)) {
            throw new IllegalArgumentException("提交过于频繁，请勿重复提交");
        }
        LocalDateTime now = LocalDateTime.now();
        // 商品价格和服务时长由服务端决定，客户端只提交商品编号。
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo("PAY" + UUID.randomUUID().toString().replace("-", ""));
        order.setRequestId(requestId);
        order.setUserId(DEMO_USER_ID);
        order.setProductCode(productCode);
        order.setSubject(PRODUCT_SUBJECT);
        order.setAmount(PRODUCT_PRICE);
        order.setStatus(PaymentOrder.STATUS_WAIT_PAY);
        order.setCreatedAt(now);
        order.setExpiresAt(now.plusMinutes(ORDER_EXPIRE_MINUTES));
        log.info("订单创建成功： {}",order);
        try {
            orders.insert(order);
            return orders.selectByOrderNo(order.getOrderNo());
        } catch (DuplicateKeyException e) {
            PaymentOrder existing = orders.selectByRequestId(requestId);
            if (existing == null || existing.getUserId() != DEMO_USER_ID
                    || !productCode.equals(existing.getProductCode())) {
                throw new IllegalArgumentException("requestId 已被其他订单使用");
            }
            return existing;
        }
    }

    public PaymentOrder getOrder(String orderNo) {
        PaymentOrder order = orders.selectByOrderNo(orderNo);
        requireOrder(order);
        return order;
    }

    @Transactional
    public String startPayment(String orderNo) {
        PaymentOrder order = orders.selectByOrderNoForUpdate(orderNo);
        requireOrder(order);
        if (!PaymentOrder.STATUS_WAIT_PAY.equals(order.getStatus())) {
            throw new IllegalArgumentException("订单已支付或已关闭");
        }
        if (!LocalDateTime.now().isBefore(order.getExpiresAt())) {
            throw new IllegalArgumentException("订单已超时，请查询支付结果后重新下单");
        }
        alipay.createTrade(order);
        return "/mock-alipay.html?orderNo=" + order.getOrderNo();
    }

    @Transactional
    public void handleNotification(Map<String, String> parameters) {
        if (!signature.verify(parameters)) {
            throw new IllegalArgumentException("支付宝通知验签失败");
        }
        if (!MockAlipayService.APP_ID.equals(parameters.get("app_id"))
                || !MockAlipayService.SELLER_ID.equals(parameters.get("seller_id"))) {
            throw new IllegalArgumentException("通知的应用或收款商户不匹配");
        }
        PaymentOrder order = orders.selectByOrderNoForUpdate(parameters.get("out_trade_no"));
        requireOrder(order);
        BigDecimal amount = new BigDecimal(parameters.get("total_amount"));
        if (order.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("通知金额与订单金额不一致");
        }
        String status = parameters.get("trade_status");
        if (MockAlipayTrade.STATUS_TRADE_SUCCESS.equals(status) || "TRADE_FINISHED".equals(status)) {
            // 更新订单状态和 订单号 和过期时间
            markPaid(order, parameters.get("trade_no"), LocalDateTime.parse(parameters.get("gmt_payment")));
            return;
        }
        if (MockAlipayTrade.STATUS_TRADE_CLOSED.equals(status)) {
            orders.updateClose(order.getOrderNo());
            return;
        }
        if (!MockAlipayTrade.STATUS_WAIT_BUYER_PAY.equals(status)) {
            throw new IllegalArgumentException("无法识别的渠道交易状态");
        }
    }

    @Transactional
    public PaymentOrder syncOrder(String orderNo) {
        PaymentOrder order = orders.selectByOrderNoForUpdate(orderNo);
        requireOrder(order);
        if (!PaymentOrder.STATUS_WAIT_PAY.equals(order.getStatus())) {
            return order;
        }
        MockAlipayTrade trade = alipay.query(orderNo);
        if (trade == null) {
            if (!LocalDateTime.now().isBefore(order.getExpiresAt())) {
                orders.updateClose(orderNo);
            }
            return orders.selectByOrderNo(orderNo);
        }
        if (order.getAmount().compareTo(trade.getAmount()) != 0) {
            throw new IllegalArgumentException("查单金额与订单金额不一致");
        }
        if (MockAlipayTrade.STATUS_TRADE_SUCCESS.equals(trade.getStatus())
                || "TRADE_FINISHED".equals(trade.getStatus())) {
            markPaid(order, trade.getTradeNo(), trade.getPaidAt());
        } else if (MockAlipayTrade.STATUS_TRADE_CLOSED.equals(trade.getStatus())) {
            // 先确认渠道已经关闭，不能只根据本地超时时间关单，避免漏掉已付款的订单。
            orders.updateClose(orderNo);
        }
        return orders.selectByOrderNo(orderNo);
    }

    public int getServiceDays() {
        String key = ENTITLEMENT_KEY + DEMO_USER_ID;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return Integer.parseInt(cached);
            }
        } catch (RuntimeException e) {
            // Redis 不可用时降级直查数据库
            log.warn("权益缓存读取失败，直查数据库", e);
        }
        int days = grants.sumServiceDays(DEMO_USER_ID);
        try {
            redis.opsForValue().set(key, String.valueOf(days), ENTITLEMENT_TTL);
        } catch (RuntimeException e) {
            log.warn("权益缓存写入失败", e);
        }
        return days;
    }

    private void markPaid(PaymentOrder order, String tradeNo, LocalDateTime paidAt) {
        if (tradeNo == null || tradeNo.isBlank() || tradeNo.length() > 64 || paidAt == null) {
            throw new IllegalArgumentException("渠道支付凭据不完整");
        }
        if (PaymentOrder.STATUS_PAID.equals(order.getStatus())) {
            if (!tradeNo.equals(order.getTradeNo())) {
                throw new IllegalArgumentException("订单的支付宝交易号不一致");
            }
            return;
        }
        if (PaymentOrder.STATUS_CLOSED.equals(order.getStatus())) {
            throw new IllegalArgumentException("已关闭订单收到付款通知，需要人工核对");
        }
        // 开始插入订单 tradeNo
        if (orders.updateMarkPaid(order.getOrderNo(), tradeNo, paidAt) == 1) {
            // 同库事务保证：订单已支付时，这笔订单对应的权益流水也已经落库。
            MailServiceGrant grant = new MailServiceGrant();
            grant.setOrderNo(order.getOrderNo());
            grant.setUserId(order.getUserId());
            grant.setServiceDays(SERVICE_DAYS);
            grant.setGrantedAt(LocalDateTime.now());
            grants.insert(grant);
            evictEntitlementCache(order.getUserId());
        }
    }

    private void requireOrder(PaymentOrder order) {
        if (order == null || order.getUserId() == null || order.getUserId() != DEMO_USER_ID) {
            throw new IllegalArgumentException("订单不存在");
        }
    }

    /** SETNX 防重复提交；Redis 不可用时放行，幂等由数据库唯一约束保证。 */
    private boolean tryMarkRequest(String requestId) {
        try {
            Boolean first = redis.opsForValue().setIfAbsent(IDEMPOTENT_KEY + requestId, "1", IDEMPOTENT_TTL);
            return !Boolean.FALSE.equals(first);
        } catch (RuntimeException e) {
            log.warn("防重复提交标记失败，放行并由数据库唯一约束兜底，requestId={}", requestId, e);
            return true;
        }
    }

    /** 缓存删除失败不影响支付结果，5 分钟 TTL 兜底最终一致。 */
    private void evictEntitlementCache(Long userId) {
        try {
            redis.delete(ENTITLEMENT_KEY + userId);
        } catch (RuntimeException e) {
            log.warn("权益缓存删除失败", e);
        }
    }
}
