package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.exception.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentId_withLongPrincipal_returnsId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(42L, null, List.of()));

        assertThat(SecurityUtils.getCurrentId()).isEqualTo(42L);
    }

    @Test
    void getCurrentId_withNonLongPrincipal_throwsUnauthorized() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-an-id", null, List.of()));

        assertThatThrownBy(SecurityUtils::getCurrentId)
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(401);
    }
}
