package org.xiaoxu.common.constants;

/**
 * Redis Key 前缀常量类
 * 统一管理所有 Redis Key 前缀
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    // ==================== Token 相关 ====================
    public static final String TOKEN_PREFIX = "login:token:";
    public static final String USER_TOKEN_PREFIX = "login:user:";

    // ==================== 登录相关 ====================
    public static final String LOGIN_FAIL_PREFIX = "login:fail:";
    public static final String LOGIN_CODE_PREFIX = "login:code:";

    // ==================== 幂等性相关 ====================
    public static final String IDEMPOTENT_PREFIX = "audit:log:processed:";

    // ==================== 过期时间 ====================
    public static final long TOKEN_EXPIRE_HOURS = 1;
    public static final long CODE_EXPIRE_MINUTES = 5;
    public static final long CODE_RATE_LIMIT_SECONDS = 60;
    public static final long IDEMPOTENT_EXPIRE_HOURS = 24;
}
