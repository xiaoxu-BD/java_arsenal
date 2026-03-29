package org.xiaoxu.ne.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.xiaoxu.ne.entity.CoParty;
import org.xiaoxu.ne.entity.CoPolicy;

import java.util.List;

@Mapper
public interface CoPolicyMapper {

    @Insert("""
        INSERT INTO insurance_co_policy
        (co_policy_no, master_policy_no, insurer_code,
         share_ratio, share_amount, share_premium,
         is_remainder, co_policy_status, sort_order, created_at)
        VALUES
        (#{coPolicyNo}, #{masterPolicyNo}, #{insurerCode},
         #{shareRatio}, #{shareAmount}, #{sharePremium},
         #{isRemainder}, #{coPolicyStatus}, #{sortOrder}, now())
    """)
    void insert(CoPolicy coPolicy);

    default void batchInsert(List<CoPolicy> list) {
        for (CoPolicy c : list) {
            insert(c); // 简化版，生产建议用 batch
        }
    }


    @Select("select * from insurance_co_policy where master_policy_no = #{mastPolicyNo} ")

    List<CoPolicy>  findByMasterPolicyNo(String mastPolicyNo);
}