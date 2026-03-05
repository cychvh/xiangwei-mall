package com.cyc.xiangwei.common.exception;

import com.cyc.xiangwei.common.utils.Result;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常统一处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截 @Validated 参数校验失败的异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        FieldError firstError = bindingResult.getFieldError();
        String errorMsg = firstError != null ? firstError.getDefaultMessage() : "参数校验失败";
        return Result.error("400", errorMsg);
    }

    /**
     * 拦截所有的 RuntimeException（例如业务层抛出的异常）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException ex) {
        // 打印异常堆栈到控制台，方便后端排查问题
        ex.printStackTrace();
        return Result.error("500", ex.getMessage());
    }
}
