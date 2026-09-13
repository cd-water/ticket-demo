package com.cdwater.cdticket.admin.common.exception;

import lombok.Getter;


/**
 * 带业务错误码的运行时异常基类
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    private final String code;

    public AbstractException(String message, String code) {
        super(message);
        this.code = code;
    }
}
