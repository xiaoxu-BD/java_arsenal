package org.xiaoxu.wecommail.common;

/**
 * 邮件发送记录状态机:
 *
 * <pre>
 *           创建(定时扫描到点,uk_group_date 防重)
 *   [无] ─────────────────► WAITING ──CAS 认领──► SENDING ──成功──► SUCCESS(终态)
 *                              ▲                     │                │
 *                              │                     ├──失败──► FAIL ──┤退避到期且
 *                              │                     │         ▲  │   │retry_count<max
 *                              │                     │         │  │   │(重新 CAS 认领)
 *                              │ 卡死超时(>阈值)置 FAIL │         │  └───┘
 *                              └─────────────────────┘◄────────┘
 *                                                    retry_count≥max → 终态 FAIL(告警)
 * </pre>
 */
public enum SendStatus {
    /** 已创建,待发送线程认领 */
    WAITING,
    /** 发送中(已被某线程/实例 CAS 认领) */
    SENDING,
    /** 发送成功(终态) */
    SUCCESS,
    /** 发送失败,保留失败原因,满足重试条件可再次认领 */
    FAIL
}
