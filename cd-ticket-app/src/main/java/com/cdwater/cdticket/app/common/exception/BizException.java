package com.cdwater.cdticket.app.common.exception;

import com.cdwater.cdticket.app.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final String code;
    /** 附加数据（如 C501 的冲突座位列表），随响应 data 返回 */
    private final Object data;

    public BizException(String message, String code) {
        this(message, code, null);
    }

    public BizException(String message, String code, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public BizException(ResultCode resultCode) {
        this(resultCode, null);
    }

    public BizException(ResultCode resultCode, Object data) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.data = data;
    }
}
