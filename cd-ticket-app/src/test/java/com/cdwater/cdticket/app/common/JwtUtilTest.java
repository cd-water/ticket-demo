package com.cdwater.cdticket.app.common;

import com.cdwater.cdticket.app.common.util.JwtUtil;
import com.cdwater.cdticket.app.infrastructure.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("cd-ticket-dev-secret-key-0123456789abcdef0123456789abcdef");
        props.setAccessExpireSeconds(900L);
        props.setRefreshExpireSeconds(604800L);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void userTokenRoundTrip() {
        String token = jwtUtil.createAccessToken(123L);
        assertEquals(123L, jwtUtil.parseUserId(token));
    }

    @Test
    void invalidTokenThrows() {
        assertThrows(JwtException.class, () -> jwtUtil.parseSubject("not-a-jwt"));
    }
}
