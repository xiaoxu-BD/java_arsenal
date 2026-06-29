package org.xiaoxu.pay.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.common.exception.BizErrorCode;
import org.xiaoxu.common.exception.BizException;
import org.xiaoxu.pay.constant.PayOrderStatus;
import org.xiaoxu.pay.entity.PayOrder;
import org.xiaoxu.pay.entity.PayRecord;
import org.xiaoxu.pay.mapper.PayOrderMapper;
import org.xiaoxu.pay.mapper.PayRecordMapper;
import org.xiaoxu.pay.service.PayService;
import org.xiaoxu.pay.vo.PayResultVO;

import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayService {

    private final PayOrderMapper payOrderMapper;
    private final PayRecordMapper payRecordMapper;
    private final AlipayClient alipayClient;
    private final FulfillmentOrderMapper fulfillmentOrderMapper;

    @Value("${alipay.appId}")
    private String appId;

    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    @Value("${alipay.returnUrl}")
    private String returnUrl;

    @Value("${alipay.publicKey}")
    private String alipayPublicKey;

    @Value("${alipay.charset}")
    private String charset;

    @Value("${alipay.signType}")
    private String signType;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final long EXPIRE_HOURS = 24;

    @Override
    @Transactional
    public PayOrder createPayOrder(String businessType, Long businessId, String processInstanceId,
                                   Long userId, String username, String productName, BigDecimal amount) {
        // 检查是否已存在付款单
        PayOrder existOrder = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getBusinessType, businessType)
                        .eq(PayOrder::getBusinessId, businessId)
                        .in(PayOrder::getStatus, PayOrderStatus.PENDING.name(), PayOrderStatus.PAID.name())
        );
        if (existOrder != null) {
            log.info("付款单已存在: orderNo={}", existOrder.getOrderNo());
            return existOrder;
        }

        // 创建付款单
        PayOrder order = new PayOrder();
        order.setOrderNo(generateOrderNo());
        order.setBusinessType(businessType);
        order.setBusinessId(businessId);
        order.setProcessInstanceId(processInstanceId);
        order.setUserId(userId);
        order.setUsername(username);
        order.setProductName(productName);
        order.setAmount(amount);
        order.setStatus(PayOrderStatus.PENDING.name());
        LocalDateTime now = LocalDateTime.now();
        order.setExpireTime(now.plusHours(EXPIRE_HOURS));
        order.setCreateTime(now);
        order.setUpdateTime(now);
        payOrderMapper.insert(order);

        log.info("付款单创建成功: orderNo={}, amount={}", order.getOrderNo(), amount);
        return order;
    }

    @Override
    @Transactional
    public PayResultVO pay(String orderNo, Long userId) {
        // 1. 查询付款单
        PayOrder order = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BizException(BizErrorCode.NOT_FOUND, "付款单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(BizErrorCode.FORBIDDEN, "无权操作此订单");
        }
        if (!PayOrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BizException(BizErrorCode.BAD_REQUEST, "订单状态异常，当前状态：" + PayOrderStatus.valueOf(order.getStatus()).getLabel());
        }
        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException(BizErrorCode.BAD_REQUEST, "订单已过期");
        }

        // 2. 调用支付宝下单接口
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setNotifyUrl(notifyUrl);
        payRequest.setReturnUrl(returnUrl);

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setTotalAmount(order.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        model.setSubject(order.getProductName());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeExpire(order.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        payRequest.setBizModel(model);

        // 3. 返回支付表单
        PayResultVO result = new PayResultVO();
        result.setOrderNo(orderNo);
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(payRequest);

            log.info("success={}", response.isSuccess());
            log.info("code={}", response.getCode());
            log.info("msg={}", response.getMsg());
            log.info("subCode={}", response.getSubCode());
            log.info("subMsg={}", response.getSubMsg());

            String payForm = response.getBody();
            log.info("payForm:{}",payForm);
            result.setPayForm(payForm);
            log.info("支付发起成功: orderNo={}", orderNo);
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败: orderNo={}, errCode={}, errMsg={}", orderNo, e.getErrCode(), e.getErrMsg(), e);
            throw new BizException(BizErrorCode.SYSTEM_ERROR, "支付下单失败：" + e.getErrMsg() + "(" + e.getErrCode() + ")");
        }
        return result;
    }

    @Override
    @Transactional
    public String handleNotify(Map<String, String> params) {
        try {
            // 1. 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);
            if (!signVerified) {
                log.error("支付回调验签失败");
                return "failure";
            }

            // 2. 获取关键参数
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            String totalAmount = params.get("total_amount");
            String buyerId = params.get("buyer_id");

            log.info("收到支付回调: orderNo={}, tradeNo={}, status={}", orderNo, tradeNo, tradeStatus);

            // 3. 查询订单
            PayOrder order = payOrderMapper.selectOne(
                    new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, orderNo)
            );
            if (order == null) {
                log.error("订单不存在: {}", orderNo);
                return "failure";
            }

            // 4. 验证金额
            if (order.getAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
                log.error("金额不匹配: order={}, alipay={}", order.getAmount(), totalAmount);
                return "failure";
            }

            // 5. 记录支付记录
            PayRecord record = new PayRecord();
            record.setOrderNo(orderNo);
            record.setAlipayTradeNo(tradeNo);
            record.setTradeStatus(tradeStatus);
            record.setTotalAmount(new BigDecimal(totalAmount));
            record.setBuyerId(buyerId);
            record.setRawData(params.toString());
            payRecordMapper.insert(record);

            // 6. 更新订单状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                order.setStatus(PayOrderStatus.PAID.name());
                order.setAlipayTradeNo(tradeNo);
                order.setPayTime(LocalDateTime.now());
                payOrderMapper.updateById(order);

                // 7. TODO: 处理业务逻辑（如标记履约单已付款）
                handleBusinessCallback(order);

                log.info("订单支付成功: {}", orderNo);
            }

            return "success";
        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            return "failure";
        }
    }

    @Override
    public IPage<PayOrder> queryUserOrders(Long userId, String status, int current, int size) {
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PayOrder::getStatus, status);
        }
        wrapper.orderByDesc(PayOrder::getCreateTime);
        return payOrderMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo, Long userId, String reason) {
        PayOrder order = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BizException(BizErrorCode.NOT_FOUND, "付款单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(BizErrorCode.FORBIDDEN, "无权操作此订单");
        }
        if (!PayOrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BizException(BizErrorCode.BAD_REQUEST, "只能取消待支付的订单");
        }

        order.setStatus(PayOrderStatus.CANCELLED.name());
        order.setCancelReason(reason);
        payOrderMapper.updateById(order);

        log.info("订单已取消: orderNo={}, reason={}", orderNo, reason);
    }

    @Override
    public String queryOrderStatus(String orderNo) {
        PayOrder order = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, orderNo)
        );
        return order != null ? order.getStatus() : null;
    }

    @Override
    public PayOrder queryOrder(String orderNo) {
        return payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, orderNo)
        );
    }

    /**
     * 处理业务回调
     */
    private void handleBusinessCallback(PayOrder order) {
        switch (order.getBusinessType()) {
            case "fulfillment":
                FulfillmentOrder fulfillmentOrder = fulfillmentOrderMapper.selectById(order.getBusinessId());
                if (fulfillmentOrder != null) {
                    fulfillmentOrder.setStatus(ApprovalStatus.COMPLETED.name());
                    fulfillmentOrderMapper.updateById(fulfillmentOrder);
                    log.info("履约单已完成: id={}, orderNo={}", fulfillmentOrder.getId(), fulfillmentOrder.getOrderNo());
                } else {
                    log.warn("履约单不存在: businessId={}", order.getBusinessId());
                }
                break;
            case "leave":
                // TODO: 处理请假扣款
                log.info("请假扣款完成: businessId={}", order.getBusinessId());
                break;
            default:
                log.warn("未知业务类型: {}", order.getBusinessType());
        }
    }

    /**
     * 清理过期的待支付订单
     */
    public int expirePendingOrders() {
        List<PayOrder> expiredOrders = payOrderMapper.selectList(
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getStatus, PayOrderStatus.PENDING.name())
                        .lt(PayOrder::getExpireTime, LocalDateTime.now())
        );
        if (expiredOrders.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (PayOrder order : expiredOrders) {
            order.setStatus(PayOrderStatus.EXPIRED.name());
            payOrderMapper.updateById(order);
            count++;
        }
        log.info("过期订单清理完成: 共{}条", count);
        return count;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "PAY" + LocalDateTime.now().format(ORDER_NO_FORMAT)
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
