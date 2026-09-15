package com.cdwater.cdticket.app.infrastructure.security;

import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long id) return id;
        throw new BizException(ResultCode.UNAUTHORIZED);
    }
}
