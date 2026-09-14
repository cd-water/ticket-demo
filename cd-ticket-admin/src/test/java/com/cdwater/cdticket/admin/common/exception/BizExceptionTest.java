package com.cdwater.cdticket.admin.common.exception;

import com.cdwater.cdticket.admin.common.ResultCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizExceptionTest {

    @Test
    void messageCodeConstructor() {
        BizException e = new BizException("boom", 418);
        assertThat(e.getMessage()).isEqualTo("boom");
        assertThat(e.getCode()).isEqualTo(418);
    }

    @Test
    void resultCodeConstructor() {
        BizException e = new BizException(ResultCode.FORBIDDEN);
        assertThat(e.getMessage()).isEqualTo(ResultCode.FORBIDDEN.getMessage());
        assertThat(e.getCode()).isEqualTo(403);
    }
}
