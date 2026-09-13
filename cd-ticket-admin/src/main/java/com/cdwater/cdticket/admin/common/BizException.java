package com.cdwater.cdticket.admin.common;

/**
 * 业务异常。用于业务逻辑校验失败、状态非法等场景。
 * 对应 HTTP 200 + body 中 code 非零。
 */
public class BizException extends AbstractException {

    public BizException(String message, String code) {
        super(message, code);
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage(), resultCode.getCode());
    }
}
