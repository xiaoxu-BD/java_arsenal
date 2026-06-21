package org.xiaoxu.workflow.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.ApproveLeave;

import java.util.List;

/**
 * 请假单 Mapper
 */
@Mapper
public interface ApproveLeaveMapper extends BaseMapper<ApproveLeave> {

    /**
     * 查询已通过或已驳回但未发送邮件通知的请假单
     */
    default List<ApproveLeave> selectUnnotified() {
        return selectList(new LambdaQueryWrapper<ApproveLeave>()
                .in(ApproveLeave::getStatus, ApprovalStatus.APPROVED.name(), ApprovalStatus.REJECTED.name())
                .eq(ApproveLeave::getNotified, "0"));
    }

    /**
     * 标记为已通知
     */
    default void markNotified(Long id) {
        update(null, new LambdaUpdateWrapper<ApproveLeave>()
                .eq(ApproveLeave::getId, id)
                .set(ApproveLeave::getNotified, "1"));
    }
}
