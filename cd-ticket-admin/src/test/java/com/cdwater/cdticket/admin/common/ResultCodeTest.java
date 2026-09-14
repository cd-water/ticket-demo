package com.cdwater.cdticket.admin.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ResultCodeTest {

    @Test
    void values_haveExpectedCodeAndMessage() {
        assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
        assertThat(ResultCode.SUCCESS.getMessage()).isEqualTo("请求成功");
        assertThat(ResultCode.BAD_REQUEST.getCode()).isEqualTo(400);
        assertThat(ResultCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ResultCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ResultCode.NOT_FOUND.getCode()).isEqualTo(404);
        assertThat(ResultCode.CONFLICT.getCode()).isEqualTo(409);
        assertThat(ResultCode.INTERNAL_ERROR.getCode()).isEqualTo(500);
    }

    @Test
    void codes_areUnique() {
        long distinctCodes = Arrays.stream(ResultCode.values())
                .map(ResultCode::getCode)
                .distinct()
                .count();
        assertThat(distinctCodes).isEqualTo(ResultCode.values().length);
    }
}
