package org.xiaoxu.ne.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.ne.entity.CoParty;
import org.xiaoxu.ne.entity.Policy;
import org.xiaoxu.ne.mapper.CoPartyMapper;
import org.xiaoxu.ne.mapper.PolicyMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolicyService {

    @Autowired
    private PolicyMapper policyMapper;

    @Autowired
    private CoPartyMapper coPartyMapper;

    @Autowired
    private CoInsuranceSplitService splitService;

    @Transactional(rollbackFor = Exception.class)
    public String issue(String draftId) {

        // 1. 构建主单
        Policy policy = new Policy();
        policy.setPolicyId(System.currentTimeMillis());
        policy.setPolicyNo("P" + System.currentTimeMillis());
        policy.setTotalPremium(new BigDecimal("10000"));
        policy.setTotalAmount(new BigDecimal("10000"));
        policy.setSplitStatus("INIT");

        policyMapper.insert(policy);

        // 2. 查询共保方
        List<CoParty> parties = coPartyMapper.findByDraftId(draftId);

        // 3. 拆单（同一事务）
        splitService.split(policy, parties);

        // 4. 更新主单状态
        policyMapper.updateSplitStatus(policy.getPolicyNo(), "SPLIT");

        return policy.getPolicyNo();
    }
}