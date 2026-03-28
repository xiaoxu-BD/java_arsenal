package org.xiaoxu.entity;

import org.xiaoxu.enums.CoPolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─── 分保单 ────────────────────────────────────────────────────────────────────
public class CoPolicy {
 
    private Long            coPolicyId;
    private String          coPolicyNo;
    private String          masterPolicyNo;
    private String          insurerCode;
    private String          insurerName;        // 快照，防止机构名变更
    private BigDecimal shareRatio;         // 原始比例（用于理赔计算）
    private BigDecimal      shareAmount;        // 分摊保险金额（精度处理后）
    private BigDecimal      sharePremium;       // 分摊保费（精度处理后，含尾差）
    private boolean         isRemainder;        // 是否为尾差承担方
    private CoPolicyStatus coPolicyStatus;
    private int             sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime   updatedAt;
 
    // ── Getters / Setters ──────────────────────────────────────────────────────
    public Long getCoPolicyId()                         { return coPolicyId; }
    public void setCoPolicyId(Long id)                  { this.coPolicyId = id; }
    public String getCoPolicyNo()                       { return coPolicyNo; }
    public void setCoPolicyNo(String no)                { this.coPolicyNo = no; }
    public String getMasterPolicyNo()                   { return masterPolicyNo; }
    public void setMasterPolicyNo(String no)            { this.masterPolicyNo = no; }
    public String getInsurerCode()                      { return insurerCode; }
    public void setInsurerCode(String code)             { this.insurerCode = code; }
    public String getInsurerName()                      { return insurerName; }
    public void setInsurerName(String name)             { this.insurerName = name; }
    public BigDecimal getShareRatio()                   { return shareRatio; }
    public void setShareRatio(BigDecimal r)             { this.shareRatio = r; }
    public BigDecimal getShareAmount()                  { return shareAmount; }
    public void setShareAmount(BigDecimal a)            { this.shareAmount = a; }
    public BigDecimal getSharePremium()                 { return sharePremium; }
    public void setSharePremium(BigDecimal p)           { this.sharePremium = p; }
    public boolean isRemainder()                        { return isRemainder; }
    public void setRemainder(boolean r)                 { this.isRemainder = r; }
    public CoPolicyStatus getCoPolicyStatus()           { return coPolicyStatus; }
    public void setCoPolicyStatus(CoPolicyStatus s)     { this.coPolicyStatus = s; }
    public int getSortOrder()                           { return sortOrder; }
    public void setSortOrder(int o)                     { this.sortOrder = o; }
    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime t)           { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)           { this.updatedAt = t; }
}