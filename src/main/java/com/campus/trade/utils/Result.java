package com.campus.trade.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码
     * 约定：200-成功, 400-请求错误, 401-未认证, 403-禁止访问, 404-未找到, 500-服务器内部错误，其他自定义业务码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    // 构造函数私有化，防止外部直接创建
    private Result() {}

    // --- 成功的静态方法 ---
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("Success");
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("Success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // --- 失败的静态方法 ---
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 便捷方法，使用常见的错误码
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> Result<T> internalError(String message) {
        return error(500, message);
    }
}