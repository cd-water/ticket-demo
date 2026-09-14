package com.cdwater.cdticket.admin.common.exception;

import com.cdwater.cdticket.admin.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBiz_returnsCodeAndMessage() {
        Result<Void> r = handler.handleBiz(new BizException("业务失败", 400));
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("业务失败");
    }

    @Test
    void handleValid_returnsFirstFieldError() {
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "name", "名字不能为空")));
        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, br);

        Result<Void> r = handler.handleValid(e);
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("名字不能为空");
    }

    @Test
    void handleValid_withoutFieldErrors_returnsDefaultMessage() {
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(List.of());
        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, br);

        Result<Void> r = handler.handleValid(e);
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("参数错误");
    }

    @Test
    void handleConstraintViolation_returnsFirstViolationMessage() {
        ConstraintViolation<?> cv = mock(ConstraintViolation.class);
        when(cv.getMessage()).thenReturn("必须大于等于 1");
        ConstraintViolationException e = new ConstraintViolationException("校验失败", Set.of(cv));

        Result<Void> r = handler.handleConstraintViolation(e);
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("必须大于等于 1");
    }

    @Test
    void handleMaxUploadSize_returns5MbMessage() {
        Result<Void> r = handler.handleMaxUploadSize(new MaxUploadSizeExceededException(1024L));
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("图片不能超过 5MB");
    }

    @Test
    void handleDuplicateKey_returnsConflict() {
        Result<Void> r = handler.handleDuplicateKey(new DuplicateKeyException("duplicate"));
        assertThat(r.getCode()).isEqualTo(409);
        assertThat(r.getMessage()).isEqualTo("数据已存在（唯一键冲突）");
    }

    @Test
    void handleMissingParam_includesParameterName() {
        Result<Void> r = handler.handleMissingParam(
                new MissingServletRequestParameterException("status", "Integer"));
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("缺少参数: status");
    }

    @Test
    void handleNotReadable_returnsBadRequest() {
        Result<Void> r = handler.handleNotReadable(mock(HttpMessageNotReadableException.class));
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("请求体缺失或格式错误");
    }

    @Test
    void handleTypeMismatch_includesParameterName() {
        MethodArgumentTypeMismatchException e = mock(MethodArgumentTypeMismatchException.class);
        when(e.getName()).thenReturn("status");
        Result<Void> r = handler.handleTypeMismatch(e);
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("参数类型错误: status");
    }

    @Test
    void handleOther_returnsInternalError() {
        Result<Void> r = handler.handleOther(new RuntimeException("boom"));
        assertThat(r.getCode()).isEqualTo(500);
        assertThat(r.getMessage()).isEqualTo("系统异常");
    }
}
