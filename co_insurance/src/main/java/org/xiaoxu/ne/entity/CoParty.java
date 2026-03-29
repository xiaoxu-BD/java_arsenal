package org.xiaoxu.ne.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 共保方配置（对应 insurance_co_party）
 *
 * 说明：
 * 1. 录单时录入
 * 2. 拆单时作为输入数据
 */
@Data
public class CoParty {

    /** 主键ID */
    private Long id;

    /** 录单草稿ID */
    private String draftId;

    /** 承保方编码 */
    private String insurerCode;

    /** 承保方名称 */
    private String insurerName;

    /**
     * 承保比例（必须保证总和=1）
     * 精度：6位小数
     */
    private BigDecimal shareRatio;

    /**
     * 排序号：
     * 决定拆单顺序
     * 最大的承担尾差
     */
    private Integer sortOrder;

    /**
     * 是否主共：
     * 1 = 主共（Lead）
     * 0 = 从共（Follow）
     */
    private Integer isLead;
}