package org.xiaoxu;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.ne.entity.CoPolicy;
import org.xiaoxu.ne.mapper.CoPolicyMapper;
import org.xiaoxu.ne.service.PolicyService;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class PolicyServiceTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private CoPolicyMapper coPolicyMapper;

    @Test
    public void testSplitAndSave() {

        // 1. 执行出单
       String policyNo =  policyService.issue("DRAFT_001");

        // 2. 查询数据库
        List<CoPolicy> list =  coPolicyMapper.findByMasterPolicyNo(policyNo); // 替换实际policyNo

        // 3. 校验数量
        assert list.size() == 3;

        // 4. 校验总金额
        BigDecimal sum = list.stream()
                .map(CoPolicy::getSharePremium)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assert sum.compareTo(new BigDecimal("10000")) == 0;

        // 5. 校验尾差
        CoPolicy last = list.get(2);
        assert last.getIsRemainder() == 1;
    }
}