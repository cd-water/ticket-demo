package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Long getCurrentId() {
        Object principal = auth().getPrincipal();
        if (principal instanceof Long id) return id;
        throw new BizException(ResultCode.UNAUTHORIZED);
    }

    public static long getCinemaId() {
        return context().cinemaId();
    }

    public static void requireScope(long cinemaId) {
        TokenAuthenticationFilter.AdminContext ctx = context();
        if (!ctx.isPlatformAdmin() && ctx.cinemaId() != cinemaId) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    private static Authentication auth() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) throw new BizException(ResultCode.UNAUTHORIZED);
        return a;
    }

    private static TokenAuthenticationFilter.AdminContext context() {
        Object details = auth().getDetails();
        if (details instanceof TokenAuthenticationFilter.AdminContext ctx) return ctx;
        throw new BizException(ResultCode.UNAUTHORIZED);
    }
}
