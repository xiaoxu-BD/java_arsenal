package org.xiaoxu.wecommail.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.wecommail.payment.entity.MockAlipayTrade;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MockAlipayTradeMapper {

    MockAlipayTrade selectByOrderNo(@Param("orderNo") String orderNo);

    MockAlipayTrade selectByOrderNoForUpdate(@Param("orderNo") String orderNo);

    int insert(MockAlipayTrade trade);

    int updatePaySuccess(@Param("orderNo") String orderNo, @Param("paidAt") LocalDateTime paidAt);

    int updateClose(@Param("orderNo") String orderNo);

    List<MockAlipayTrade> selectPendingNotifications(@Param("limit") int limit);

    int updateNotifyAck(@Param("orderNo") String orderNo);
}
