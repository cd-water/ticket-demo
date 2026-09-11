package com.cdwater.cdticket.common.infrastructure;

import com.cdwater.cdticket.common.domain.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ResultWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ResultWriter() {}

    public static String toJson(Result<?> result) {
        try {
            return MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"code\":500,\"message\":\"json serialize error\",\"data\":null}";
        }
    }
}
