package org.xiaoxu.enums;

public enum SplitStatus {
    /** 非共保保单，无需拆单 */
    NONE,
    /** 已出主单，分保单生成中（异步拆单中间状态） */
    PENDING,
    /** 分保单已全部生成，等待各方确认 */
    SPLIT,
    /** 所有分保单已激活 */
    ALL_ACTIVE,
    /** 部分已激活，部分仍在草稿 */
    PARTIAL_ACTIVE,
    /** 异常：存在已激活与已取消并存 */
    EXCEPTION
}
 