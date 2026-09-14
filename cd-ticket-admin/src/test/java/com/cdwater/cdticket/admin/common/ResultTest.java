package com.cdwater.cdticket.admin.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void success_withoutData() {
        Result<Void> r = Result.success();
        assertThat(r.getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(r.getMessage()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(r.getData()).isNull();
    }

    @Test
    void success_withData() {
        Result<String> r = Result.success("hello");
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getData()).isEqualTo("hello");
    }

    @Test
    void fail_withCodeAndMessage() {
        Result<Void> r = Result.fail(400, "参数错误");
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("参数错误");
        assertThat(r.getData()).isNull();
    }

    @Test
    void fail_withResultCode() {
        Result<Void> r = Result.fail(ResultCode.NOT_FOUND);
        assertThat(r.getCode()).isEqualTo(404);
        assertThat(r.getMessage()).isEqualTo(ResultCode.NOT_FOUND.getMessage());
    }

    @Test
    void toJson_serializesEnvelope() {
        String json = Result.success("hello").toJson();
        assertThat(json).contains("\"code\":200");
        assertThat(json).contains("\"message\":\"请求成功\"");
        assertThat(json).contains("\"data\":\"hello\"");
    }
}
