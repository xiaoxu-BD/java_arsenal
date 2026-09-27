package org.xiaoxu.wecommail.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一响应结构
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "ok", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "ok", null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
