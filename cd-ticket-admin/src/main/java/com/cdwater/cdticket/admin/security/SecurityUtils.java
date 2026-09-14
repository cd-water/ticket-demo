package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录管理员工具
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
