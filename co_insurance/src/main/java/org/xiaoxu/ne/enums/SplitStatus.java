package org.xiaoxu.ne.enums;

public enum SplitStatus {

    NONE,           // 非共保
    PENDING,        // 待拆单
    SPLIT,          // 已拆单
    ALL_ACTIVE,     // 全部生效
    PARTIAL_ACTIVE, // 部分生效
    EXCEPTION       // 异常
}