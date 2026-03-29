package org.xiaoxu.ne.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.xiaoxu.ne.entity.Policy;

@Mapper
public interface PolicyMapper {

    @Insert("""
        INSERT INTO insurance_policy(policy_id, policy_no, total_amount, total_premium, split_status, created_at)
        VALUES(#{policyId}, #{policyNo}, #{totalAmount}, #{totalPremium}, #{splitStatus}, now())
    """)
    void insert(Policy policy);

    @Update("""
        UPDATE insurance_policy 
        SET split_status = #{splitStatus}
        WHERE policy_no = #{policyNo}
    """)
    void updateSplitStatus(@Param("policyNo") String policyNo,
                           @Param("splitStatus") String splitStatus);
}