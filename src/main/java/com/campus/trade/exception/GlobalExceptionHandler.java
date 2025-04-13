package com.campus.trade.exception;

import com.campus.trade.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice // 捕获 Controller 层抛出的异常
public class GlobalExceptionHandler {

    /**
     * 处理所有未被特定处理的运行时异常
     * @param e 异常对象
     * @return 统一错误响应
     */
    @ExceptionHandler(RuntimeException.class) // 捕获 RuntimeException 及其子类
    public Result<Object> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e); // 记录详细错误日志
        // 返回通用的服务器内部错误响应
        return Result.internalError("服务器内部错误，请联系管理员");
    }

    /**
     * 处理其他所有未捕获的异常
     * @param e 异常对象
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class) // 捕获所有 Exception
    public Result<Object> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e); // 记录详细错误日志
        return Result.internalError("系统繁忙，请稍后再试");
    }

    // --- 后续可以添加更多特定异常的处理 ---
    // 例如：处理业务异常 BusinessException
    // @ExceptionHandler(BusinessException.class)
    // public Result<Object> handleBusinessException(BusinessException e) {
    //     log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
    //     return Result.error(e.getCode(), e.getMessage());
    // }

    // 例如：处理参数校验异常 BindException 或 MethodArgumentNotValidException
    // @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    // public Result<Object> handleValidationException(Exception e) {
    //     String message = "参数校验失败";
    //     if (e instanceof BindException) {
    //         // 从 BindException 获取具体错误信息
    //         message = ((BindException) e).getBindingResult().getAllErrors().get(0).getDefaultMessage();
    //     } else if (e instanceof MethodArgumentNotValidException) {
    //         // 从 MethodArgumentNotValidException 获取具体错误信息
    //         message = ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors().get(0).getDefaultMessage();
    //     }
    //     log.warn("参数校验异常: {}", message);
    //     return Result.badRequest(message);
    // }
}