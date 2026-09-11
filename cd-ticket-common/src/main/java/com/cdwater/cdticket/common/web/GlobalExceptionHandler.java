package com.cdwater.cdticket.common.web;

import com.cdwater.cdticket.common.exception.AbstractException;
import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.api.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 抽象业务异常（覆盖所有继承 AbstractException 的子类，如 BizException） */
    @ExceptionHandler(AbstractException.class)
    public Result<Void> handleBiz(AbstractException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @Valid + request body 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 方法参数 @Validated 校验失败（如 @Min/@Max/@NotNull） */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 缺少必填请求参数（如 @RequestParam 漏传） */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "参数 '" + e.getParameterName() + "' 不能为空");
    }

    /** 请求体 JSON 解析失败 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 不支持的 HTTP 方法 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "不支持 " + e.getMethod() + " 方法");
    }

    /** 不支持的媒体类型 */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Void> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "Content-Type 必须为 application/json");
    }

    /** 404（已配置 server.error.include-message=never 时兜底） */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandler(NoHandlerFoundException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "接口不存在: " + e.getHttpMethod() + " " + e.getRequestURL());
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("unexpected error", e);
        return Result.fail(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMessage());
    }
}