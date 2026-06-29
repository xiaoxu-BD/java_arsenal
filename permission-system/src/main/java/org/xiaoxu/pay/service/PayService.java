package org.xiaoxu.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.pay.entity.PayOrder;
import org.xiaoxu.pay.vo.PayResultVO;

/**
 * 支付服务接口
 */
public interface PayService extends IService<PayOrder> {

    /**
     * 创建付款单（审批通过时调用）
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param processInstanceId 流程实例ID
     * @param userId 用户ID
     * @param username 用户名
     * @param productName 商品名称
     * @param amount 金额
     * @return 付款单
     */
    PayOrder createPayOrder(String businessType, Long businessId, String processInstanceId,
                            Long userId, String username, String productName, java.math.BigDecimal amount);

    /**
     * 发起支付（返回支付表单）
     * @param orderNo 付款单号
     * @param userId 用户ID
     * @return 支付结果
     */
    PayResultVO pay(String orderNo, Long userId);

    /**
     * 支付宝异步回调
     * @param params 回调参数
     * @return 处理结果
     */
    String handleNotify(java.util.Map<String, String> params);

    /**
     * 查询用户的付款单列表
     * @param userId 用户ID
     * @param status 状态（可选）
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<PayOrder> queryUserOrders(Long userId, String status, int current, int size);

    /**
     * 取消付款单
     * @param orderNo 付款单号
     * @param userId 用户ID
     * @param reason 取消原因
     */
    void cancelOrder(String orderNo, Long userId, String reason);

    /**
     * 查询订单状态
     * @param orderNo 付款单号
     * @return 状态
     */
    String queryOrderStatus(String orderNo);

    /**
     * 查询订单详情
     * @param orderNo 付款单号
     * @return 订单
     */
    PayOrder queryOrder(String orderNo);
}
