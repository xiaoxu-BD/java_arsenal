package org.xiaoxu.wecommail.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.wecommail.payment.entity.PaymentOrder;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentOrderMapper {

    PaymentOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /** 带 FOR UPDATE 行锁，用于事务内串行化同一订单的状态变更 */
    PaymentOrder selectByOrderNoForUpdate(@Param("orderNo") String orderNo);

    PaymentOrder selectByRequestId(@Param("requestId") String requestId);

    List<PaymentOrder> selectWaitingOrders(@Param("limit") int limit);

    int insert(PaymentOrder order);

    /** 带 status 条件的守护更新：只有 WAIT_PAY 才能改成 PAID，返回影响行数 */
    int updateMarkPaid(@Param("orderNo") String orderNo, @Param("tradeNo") String tradeNo,
                       @Param("paidAt") LocalDateTime paidAt);

    int updateClose(@Param("orderNo") String orderNo);
}
