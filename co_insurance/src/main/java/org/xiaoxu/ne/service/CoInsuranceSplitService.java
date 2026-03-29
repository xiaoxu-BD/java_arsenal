package org.xiaoxu.ne.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.ne.al.PremiumSplitter;
import org.xiaoxu.ne.entity.CoParty;
import org.xiaoxu.ne.entity.CoPolicy;
import org.xiaoxu.ne.entity.Policy;
import org.xiaoxu.ne.mapper.CoPolicyMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CoInsuranceSplitService {

    @Autowired
    private CoPolicyMapper coPolicyMapper;

    /**
     * 必须在事务中执行
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void split(Policy policy, List<CoParty> parties) {

        // 1. 校验比例
        validateRatio(parties);

        // 2. 排序
        parties.sort(Comparator.comparingInt(CoParty::getSortOrder));

        // 3. 拆分金额
        List<BigDecimal> premiumList =
                PremiumSplitter.split(policy.getTotalPremium(), parties);

        List<BigDecimal> amountList =
                PremiumSplitter.split(policy.getTotalAmount(), parties);

        // 4. 构建子单
        List<CoPolicy> list = new ArrayList<>();

        for (int i = 0; i < parties.size(); i++) {

            CoParty p = parties.get(i);

            CoPolicy co = new CoPolicy();
            co.setCoPolicyNo(policy.getPolicyNo() + "-" + (i + 1));
            co.setMasterPolicyNo(policy.getPolicyNo());
            co.setInsurerCode(p.getInsurerCode());

            co.setShareRatio(p.getShareRatio());
            co.setSharePremium(premiumList.get(i));
            co.setShareAmount(amountList.get(i));
            co.setIsRemainder(i == parties.size() - 1 ? 1 : 0);
            co.setCoPolicyStatus("DRAFT");
            co.setSortOrder(p.getSortOrder());

            list.add(co);
        }

        // 5. 批量落库（唯一索引兜底幂等）
        try {
            coPolicyMapper.batchInsert(list);
        } catch (DuplicateKeyException e) {
            // 幂等处理
            return;
        }

        policy.setSplitStatus("SPLIT");
    }

    private void validateRatio(List<CoParty> parties) {
        BigDecimal sum = parties.stream()
                .map(CoParty::getShareRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new RuntimeException("比例不等于1");
        }
    }
}