package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.exception.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityUtils 直接读 SecurityContext，通过塞 Authentication 模拟当前管理员
 */
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authAs(long adminId, int role, long cinemaId) {
        var auth = new UsernamePasswordAuthenticationToken(adminId, null, List.of());
        auth.setDetails(new TokenAuthenticationFilter.AdminContext(adminId, role, cinemaId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void isPlatformAdminTrueForRole0() {
        authAs(1L, 0, 0L);
        assertTrue(SecurityUtils.isPlatformAdmin());
    }

    @Test
    void isPlatformAdminFalseForRole1() {
        authAs(2L, 1, 5L);
        assertFalse(SecurityUtils.isPlatformAdmin());
    }

    @Test
    void getRoleAndCinemaIdFromContext() {
        authAs(2L, 1, 5L);
        assertEquals(1, SecurityUtils.getRole());
        assertEquals(5L, SecurityUtils.getCinemaId());
    }

    @Test
    void getCurrentIdReturnsPrincipal() {
        authAs(7L, 0, 0L);
        assertEquals(7L, SecurityUtils.getCurrentId());
    }

    @Test
    void requireScopePlatformAdminAlwaysPasses() {
        authAs(1L, 0, 0L);
        assertDoesNotThrow(() -> SecurityUtils.requireScope(99L));
    }

    @Test
    void requireScopeCinemaAdminMatches() {
        authAs(2L, 1, 5L);
        assertDoesNotThrow(() -> SecurityUtils.requireScope(5L));
        assertThrows(BizException.class, () -> SecurityUtils.requireScope(9L));
    }

    @Test
    void unauthenticatedThrowsUnauthorized() {
        SecurityContextHolder.clearContext();
        assertThrows(BizException.class, SecurityUtils::getCurrentId);
    }
}