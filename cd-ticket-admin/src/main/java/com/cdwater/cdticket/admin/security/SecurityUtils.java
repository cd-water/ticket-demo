package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录管理员身份读取（入口端校验用 @PreAuthorize；实体归属比对用 requireScope）
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 当前登录管理员 id */
    public static Long getCurrentId() {
        Object principal = auth().getPrincipal();
        if (principal instanceof Long id) return id;
        throw new BizException(ResultCode.UNAUTHORIZED);
    }

    /** 当前登录管理员 role */
    public static int getRole() {
        return context().role();
    }

    /** 当前登录管理员 cinemaId */
    public static long getCinemaId() {
        return context().cinemaId();
    }

    /** 当前是否平台管理员 */
    public static boolean isPlatformAdmin() {
        return getRole() == 0;
    }

    /** 实体加载后比对：平台管理员恒通过；影院管理员必须匹配 cinemaId */
    public static void requireScope(long cinemaId) {
        if (!isPlatformAdmin() && getCinemaId() != cinemaId) {
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