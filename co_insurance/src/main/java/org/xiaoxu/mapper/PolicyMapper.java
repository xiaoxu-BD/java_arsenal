package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.entity.Policy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PolicyMapper {
    int insert(Policy policy);
    int updateStatus(Policy policy);
    int updateSplitStatus(Policy policy);
    int updateStatusAndSplitStatus(Policy policy);
 
    Policy selectByPolicyNoForUpdate(@Param("policyNo") String policyNo);
    Policy selectByPolicyNo(@Param("policyNo") String policyNo);
    Policy selectByDraftId(@Param("draftId") String draftId);
    Policy selectByDraftIdAndPaymentBatchNo(
            @Param("draftId") String draftId,
            @Param("paymentBatchNo") String paymentBatchNo);
 
    List<Policy> selectPendingSplitPolicies(
            @Param("beforeTime") LocalDateTime beforeTime,
            @Param("limit") int limit);
}
 