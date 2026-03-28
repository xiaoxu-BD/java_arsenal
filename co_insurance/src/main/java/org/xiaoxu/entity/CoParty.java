package org.xiaoxu.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─── 共保方配置（录单时录入）──────────────────────────────────────────────────
public class CoParty {
 
    private Long        partyId;
    private String      draftId;
    private String      insurerCode;
    private String      insurerName;
    private BigDecimal shareRatio;
    private boolean     isLead;
    private int         sortOrder;
    private LocalDateTime createdAt;
 
    public Long getPartyId()                    { return partyId; }
    public void setPartyId(Long id)             { this.partyId = id; }
    public String getDraftId()                  { return draftId; }
    public void setDraftId(String id)           { this.draftId = id; }
    public String getInsurerCode()              { return insurerCode; }
    public void setInsurerCode(String code)     { this.insurerCode = code; }
    public String getInsurerName()              { return insurerName; }
    public void setInsurerName(String name)     { this.insurerName = name; }
    public BigDecimal getShareRatio()           { return shareRatio; }
    public void setShareRatio(BigDecimal r)     { this.shareRatio = r; }
    public boolean isLead()                     { return isLead; }
    public void setLead(boolean lead)           { this.isLead = lead; }
    public int getSortOrder()                   { return sortOrder; }
    public void setSortOrder(int o)             { this.sortOrder = o; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
}
 