package org.xiaoxu.workflow.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.FulfillmentOrder;

import java.util.List;

/**
 * 履约单 Mapper
 */
@Mapper
public interface FulfillmentOrderMapper extends BaseMapper<FulfillmentOrder> {

    /**
     * 查询已通过但未发送邮件通知的履约单
     */
    default List<FulfillmentOrder> selectUnnotifiedApproved() {
        return selectList(new LambdaQueryWrapper<FulfillmentOrder>()
                .eq(FulfillmentOrder::getStatus, ApprovalStatus.APPROVED.name())
                .eq(FulfillmentOrder::getNotified, "0"));
    }

    /**
     * 标记为已通知
     */
    default void markNotified(Long id) {
        update(null, new LambdaUpdateWrapper<FulfillmentOrder>()
                .eq(FulfillmentOrder::getId, id)
                .set(FulfillmentOrder::getNotified, "1"));
    }
}
