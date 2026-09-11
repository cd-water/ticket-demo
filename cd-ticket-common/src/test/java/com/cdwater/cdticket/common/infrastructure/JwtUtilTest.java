package com.cdwater.cdticket.common.infrastructure;

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
        props.setAccessExpireSeconds(900);
        props.setRefreshExpireSeconds(604800);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void userTokenRoundTrip() {
        String token = jwtUtil.createUserAccessToken(123L);
        assertEquals(123L, jwtUtil.parseUserId(token));
    }

    @Test
    void adminTokenRoundTrip() {
        String token = jwtUtil.createAdminAccessToken(7L);
        assertEquals(7L, jwtUtil.parseAdminId(token));
    }

    @Test
    void userTokenCannotBeParsedAsAdmin() {
        String token = jwtUtil.createUserAccessToken(1L);
        assertThrows(JwtException.class, () -> jwtUtil.parseAdminId(token));
    }

    @Test
    void invalidTokenThrows() {
        assertThrows(JwtException.class, () -> jwtUtil.parseSubject("not-a-jwt"));
    }
}
