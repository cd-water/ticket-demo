package com.cdwater.cdticket.admin.common;

import lombok.Getter;

/**
 * 抽象业务异常基类（阿里异常体系规范）。所有业务/参数异常均应继承本类。
 * 错误码采用 5 位数字字符串（参见 ResultCode）。
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    private final String code;

    public AbstractException(String message, String code) {
        super(message);
        this.code = code;
    }
}
