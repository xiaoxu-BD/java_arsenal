package org.xiaoxu.common.exception;


/**
 * 业务错误码枚举
 */
public enum BizErrorCode implements ErrorCode {

    // ==================== 通用错误 ====================
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ==================== 用户相关 ====================
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    OLD_PASSWORD_ERROR(1004, "旧密码不正确"),
    ACCOUNT_LOCKED(1005, "账号已锁定"),
    ACCOUNT_DISABLED(1006, "账号已禁用"),
    FIRST_LOGIN_REQUIRED(1007, "首次登录请修改密码"),

    // ==================== 验证码相关 ====================
    CAPTCHA_EXPIRED(2001, "验证码已过期"),
    CAPTCHA_ERROR(2002, "验证码错误"),
    CAPTCHA_RATE_LIMIT(2003, "验证码发送过于频繁"),

    // ==================== 角色权限相关 ====================
    ROLE_NOT_FOUND(3001, "角色不存在"),
    ROLE_ALREADY_EXISTS(3002, "角色已存在"),
    PERMISSION_DENIED(3003, "权限不足"),

    // ==================== 菜单相关 ====================
    MENU_NOT_FOUND(4001, "菜单不存在"),
    MENU_HAS_CHILDREN(4002, "菜单下有子菜单，不能删除"),

    // ==================== 公告相关 ====================
    ANNOUNCEMENT_NOT_FOUND(5001, "公告不存在"),
    ANNOUNCEMENT_STATUS_ERROR(5002, "公告状态不正确"),

    // ==================== 工作流相关 ====================
    PROCESS_DEFINITION_NOT_FOUND(6001, "流程定义不存在"),
    PROCESS_INSTANCE_NOT_FOUND(6002, "流程实例不存在"),
    TASK_NOT_FOUND(6003, "任务不存在"),
    TASK_ALREADY_CLAIMED(6004, "任务已被认领"),
    APPROVAL_FAILED(6005, "审批失败"),

    // ==================== 请假相关 ====================
    LEAVE_NOT_FOUND(7001, "请假单不存在"),
    LEAVE_STATUS_ERROR(7002, "请假单状态不正确"),
    LEAVE_ALREADY_SUBMITTED(7003, "请假单已提交审批"),

    // ==================== 履约单相关 ====================
    FULFILLMENT_NOT_FOUND(8001, "履约单不存在"),
    FULFILLMENT_STATUS_ERROR(8002, "履约单状态不正确"),
    FULFILLMENT_ALREADY_SUBMITTED(8003, "履约单已提交审批"),

    // ==================== 部门相关 ====================
    DEPARTMENT_NOT_FOUND(9001, "部门不存在"),

    // ==================== MQ 相关 ====================
    MESSAGE_PROCESS_ERROR(10001, "消息处理失败"),
    MESSAGE_ACK_ERROR(10002, "消息确认失败");

    private final int code;
    private final String message;

    BizErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return String.valueOf(code);
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 获取状态码
     */
    public int getCodeValue() {
        return code;
    }
}
