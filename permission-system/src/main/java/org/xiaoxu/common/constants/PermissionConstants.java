package org.xiaoxu.common.constants;

/**
 * 权限码常量类
 * 统一管理所有权限码字符串
 */
public final class PermissionConstants {

    private PermissionConstants() {}

    // ==================== 用户管理 ====================
    public static final String USER_CREATE = "system:user:create";
    public static final String USER_DELETE = "system:user:delete";
    public static final String USER_QUERY  = "system:user:query";
    public static final String USER_UPDATE = "system:user:update";

    // ==================== 角色管理 ====================
    public static final String ROLE_CREATE = "system:role:create";
    public static final String ROLE_DELETE = "system:role:delete";
    public static final String ROLE_QUERY  = "system:role:query";
    public static final String ROLE_UPDATE = "system:role:update";

    // ==================== 菜单管理 ====================
    public static final String MENU_CREATE = "system:menu:create";
    public static final String MENU_DELETE = "system:menu:delete";
    public static final String MENU_QUERY  = "system:menu:query";
    public static final String MENU_UPDATE = "system:menu:update";

    // ==================== 公告管理 ====================
    public static final String ANNOUNCEMENT_CREATE = "system:announcement:create";
    public static final String ANNOUNCEMENT_DELETE = "system:announcement:delete";
    public static final String ANNOUNCEMENT_QUERY  = "system:announcement:query";
    public static final String ANNOUNCEMENT_UPDATE = "system:announcement:update";

    // ==================== 工作流管理 ====================
    public static final String WORKFLOW_ADMIN_LIST = "workflow:admin:list";
}
