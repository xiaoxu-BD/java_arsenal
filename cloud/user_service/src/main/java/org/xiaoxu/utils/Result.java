package org.xiaoxu.utils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用返回结果类
 * @param <T> data的类型
 */
@Data
@Getter
@Setter
public class Result<T> implements Serializable {

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 数据封装 */
    private T data;

    /** 服务器响应时间戳 */
    private long timestamp;

    /**
     * 私有化构造函数，强制使用静态工厂方法创建实例
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // --- 静态工厂方法 ---

    /**
     * 成功，并返回数据
     *
     * @param data 数据
     * @param <T>  数据的泛型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data) {
        // 通常成功的 code 为 200 或 0，这里我们用 200
        return new Result<>(200, "Success", data);
    }

    /**
     * 成功，不返回数据（例如：创建、更新、删除等操作）
     *
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功，并返回自定义消息和数据
     *
     * @param data 数据
     * @param message 自定义消息
     * @param <T>  数据的泛型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败，返回指定的状态码和错误信息
     *
     * @param code    状态码
     * @param message 错误信息
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败，使用默认的错误状态码（例如：500）
     *
     * @param message 错误信息
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> error(String message) {
        // 通用错误码使用 500
        return new Result<>(500, message, null);
    }

    // --- Getters ---

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }



    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

}