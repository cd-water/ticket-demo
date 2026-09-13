package com.cdwater.cdticket.admin.common.exception;

import com.cdwater.cdticket.admin.common.ResultCode;


/**
 * 业务异常
 */
public class BizException extends AbstractException {

    public BizException(String message, String code) {
        super(message, code);
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage(), resultCode.getCode());
    }
}
