package com.cdwater.cdticket.admin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;

/**
 * Security 工具类
 */
public final class SecurityUtils {
    private SecurityUtils() {
    }

    /**
     * 当前登录管理员 ID
     */
    public static Long getCurrentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long id)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return id;
    }
}
