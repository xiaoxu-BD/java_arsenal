package org.xiaoxu.ne.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分保单（对应 insurance_co_policy）
 *
 * 说明：
 * 1. 每个承保方一条记录
 * 2. 与主保单通过 masterPolicyNo 关联
 */
@Data
public class CoPolicy {

    /** 主键ID */
    private Long coPolicyId;

    /** 分保单号（主单号 + 序号） */
    private String coPolicyNo;

    /** 关联主保单号 */
    private String masterPolicyNo;

    /** 承保方编码 */
    private String insurerCode;

    /** 承保方名称（快照，防止后续变更） */
    private String insurerName;

    /**
     * 承保比例（原始值）
     * 示例：0.400000 表示 40%
     */
    private BigDecimal shareRatio;

    /**
     * 分摊保险金额
     * = totalAmount × shareRatio（经过精度处理）
     */
    private BigDecimal shareAmount;

    /**
     * 分摊保费（最终值）
     * 使用 FLOOR + 尾差补偿算法计算
     */
    private BigDecimal sharePremium;

    /**
     * 是否尾差承担方：
     * 1 = 是（最后一个）
     * 0 = 否
     */
    private Integer isRemainder;

    /**
     * 分保单状态：
     * DRAFT：已生成待确认
     * ACTIVE：已生效
     * CANCELLED：已取消
     */
    private String coPolicyStatus;

    /**
     * 排序号：
     * 决定拆分顺序（最后一个承担尾差）
     */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;
}