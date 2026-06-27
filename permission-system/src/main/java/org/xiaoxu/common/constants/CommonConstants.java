package org.xiaoxu.common.constants;

/**
 * 通用常量类
 * 统一管理软删除、状态等魔法值
 */
public final class CommonConstants {

    private CommonConstants() {}

    // ==================== 软删除标记 ====================
    public static final String NOT_DELETED = "0";
    public static final String DELETED = "1";

    // ==================== 首次登录标记 ====================
    public static final String FIRST_LOGIN_YES = "1";
    public static final String FIRST_LOGIN_NO = "0";

    // ==================== 已通知标记 ====================
    public static final String NOTIFIED = "1";
    public static final String NOT_NOTIFIED = "0";

    // ==================== 用户状态 ====================
    public static final Long USER_STATUS_ACTIVE = 0L;
    public static final Long USER_STATUS_DISABLED = 1L;

    // ==================== 公告状态 ====================
    public static final int ANNOUNCEMENT_DRAFT = 0;
    public static final int ANNOUNCEMENT_PUBLISHED = 1;
    public static final int ANNOUNCEMENT_WITHDRAWN = 2;

    // ==================== 日志写入类型 ====================
    public static final int WRITE_TYPE_SYNC = 0;
    public static final int WRITE_TYPE_ASYNC = 1;

    // ==================== 日志状态 ====================
    public static final int LOG_STATUS_SUCCESS = 0;
    public static final int LOG_STATUS_FAILURE = 1;

    // ==================== 登录类型 ====================
    public static final String LOGIN_TYPE_PASSWORD = "PASSWORD";
    public static final String LOGIN_TYPE_EMAIL = "EMAIL";
    public static final String LOGIN_TYPE_LOGOUT = "LOGOUT";

    // ==================== 主部门标记 ====================
    public static final int IS_PRIMARY = 1;
    public static final int IS_NOT_PRIMARY = 0;

    // ==================== 默认角色ID ====================
    public static final Long DEFAULT_ROLE_ID = 2L;

    // ==================== 密码最小长度 ====================
    public static final int MIN_PASSWORD_LENGTH = 6;

    // ==================== 分页默认值 ====================
    public static final int DEFAULT_PAGE_CURRENT = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_LATEST_LIMIT = 3;

    // ==================== 日志截断长度 ====================
    public static final int MAX_LOG_PARAM_LENGTH = 2000;

    // ==================== HTTP 常量 ====================
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HEADER_X_REAL_IP = "X-Real-IP";
    public static final String UNKNOWN_IP = "unknown";

    // ==================== BPMN 资源后缀 ====================
    public static final String BPMN_RESOURCE_SUFFIX = ".bpmn20.xml";

    // ==================== 默认值 ====================
    public static final String DEFAULT_DELETE_REASON = "管理员手动删除";
    public static final String DEFAULT_PROCESS_NAME = "未命名流程";
}
