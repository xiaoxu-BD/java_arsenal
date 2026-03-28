package org.xiaoxu.entity;

import org.xiaoxu.enums.PolicyStatus;
import org.xiaoxu.enums.SplitStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Policy {
 
    private Long          policyId;
    private String        policyNo;
    private String        draftId;
    private String        paymentBatchNo;
    private CoInsureType  coInsureType;
    private String        productType;
    private String        insuredName;
    private BigDecimal    totalAmount;
    private BigDecimal    totalPremium;
    private BigDecimal    ownRatio;
    private BigDecimal ownPremium;
    private PolicyStatus  policyStatus;
    private SplitStatus splitStatus;
    private String        policySnapshot;   // JSON string
    private LocalDate insuranceStart;
    private LocalDate     insuranceEnd;
    private Long          createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // ── 状态机流转矩阵（集中定义）──────────────────────────────────────────────
    private static final Map<PolicyStatus, Set<PolicyStatus>> TRANSITIONS;
    static {
        Map<PolicyStatus, Set<PolicyStatus>> m = new EnumMap<>(PolicyStatus.class);
        m.put(PolicyStatus.DRAFT,        EnumSet.of(PolicyStatus.UNDERWRITING, PolicyStatus.CANCELLED));
        m.put(PolicyStatus.UNDERWRITING, EnumSet.of(PolicyStatus.ACTIVE, PolicyStatus.REJECTED, PolicyStatus.CANCELLED));
        m.put(PolicyStatus.ACTIVE,       EnumSet.of(PolicyStatus.ENDORSED, PolicyStatus.CANCELLED, PolicyStatus.EXPIRED));
        m.put(PolicyStatus.ENDORSED,     EnumSet.of(PolicyStatus.ENDORSED,  PolicyStatus.CANCELLED, PolicyStatus.EXPIRED));
        TRANSITIONS = Collections.unmodifiableMap(m);
    }
 
    /** 唯一状态变更入口，非法流转直接抛异常 */
    public void transition(PolicyStatus target) {
        Set<PolicyStatus> allowed = TRANSITIONS.getOrDefault(this.policyStatus, Collections.emptySet());
        if (!allowed.contains(target)) {
            throw new IllegalPolicyTransitionException(
                String.format("保单状态非法流转 [%s -> %s] policyNo=%s",
                    this.policyStatus, target, this.policyNo));
        }
        this.policyStatus = target;
    }
 
    public boolean isCoInsurance() {
        return coInsureType == CoInsureType.LEAD || coInsureType == CoInsureType.FOLLOW;
    }
 
    public boolean canSplit() {
        return isCoInsurance() && (splitStatus == SplitStatus.NONE || splitStatus == SplitStatus.PENDING);
    }
 
    // ── Getters / Setters ──────────────────────────────────────────────────────
    public Long getPolicyId()                       { return policyId; }
    public void setPolicyId(Long policyId)          { this.policyId = policyId; }
    public String getPolicyNo()                     { return policyNo; }
    public void setPolicyNo(String policyNo)        { this.policyNo = policyNo; }
    public String getDraftId()                      { return draftId; }
    public void setDraftId(String draftId)          { this.draftId = draftId; }
    public String getPaymentBatchNo()               { return paymentBatchNo; }
    public void setPaymentBatchNo(String no)        { this.paymentBatchNo = no; }
    public CoInsureType getCoInsureType()           { return coInsureType; }
    public void setCoInsureType(CoInsureType t)     { this.coInsureType = t; }
    public String getProductType()                  { return productType; }
    public void setProductType(String t)            { this.productType = t; }
    public String getInsuredName()                  { return insuredName; }
    public void setInsuredName(String n)            { this.insuredName = n; }
    public BigDecimal getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(BigDecimal a)        { this.totalAmount = a; }
    public BigDecimal getTotalPremium()             { return totalPremium; }
    public void setTotalPremium(BigDecimal p)       { this.totalPremium = p; }
    public BigDecimal getOwnRatio()                 { return ownRatio; }
    public void setOwnRatio(BigDecimal r)           { this.ownRatio = r; }
    public BigDecimal getOwnPremium()               { return ownPremium; }
    public void setOwnPremium(BigDecimal p)         { this.ownPremium = p; }
    public PolicyStatus getPolicyStatus()           { return policyStatus; }
    public void setPolicyStatus(PolicyStatus s)     { this.policyStatus = s; }
    public SplitStatus getSplitStatus()             { return splitStatus; }
    public void setSplitStatus(SplitStatus s)       { this.splitStatus = s; }
    public String getPolicySnapshot()               { return policySnapshot; }
    public void setPolicySnapshot(String s)         { this.policySnapshot = s; }
    public LocalDate getInsuranceStart()            { return insuranceStart; }
    public void setInsuranceStart(LocalDate d)      { this.insuranceStart = d; }
    public LocalDate getInsuranceEnd()              { return insuranceEnd; }
    public void setInsuranceEnd(LocalDate d)        { this.insuranceEnd = d; }
    public Long getCreatedBy()                      { return createdBy; }
    public void setCreatedBy(Long id)               { this.createdBy = id; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)       { this.updatedAt = t; }
}