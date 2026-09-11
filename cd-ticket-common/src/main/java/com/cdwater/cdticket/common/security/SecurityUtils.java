package com.cdwater.cdticket.common.security;

import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    /** 返回当前登录主体 ID（user 或 admin 的 Long 主键）；未认证抛 1002。 */
    public static Long getCurrentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long id)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return id;
    }
}
