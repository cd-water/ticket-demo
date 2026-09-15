package com.cdwater.cdticket.app.interfaces.auth;

import com.cdwater.cdticket.app.common.Result;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.infrastructure.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 滑动窗口限流：座位图接口按排场限流（1s 20 次，防刷）；创建订单按用户限流（60s 10 次）。
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String SEATS_PATTERN = "/api/user/screenings/";

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.contains(SEATS_PATTERN) && uri.endsWith("/seats")) {
            Long screeningId = parseScreeningId(uri);
            if (screeningId != null
                    && !rateLimitService.allow("rl:seats:screening:" + screeningId, 1, 20)) {
                writeLimited(response);
                return;
            }
        } else if ("/api/user/orders".equals(uri) && "POST".equals(request.getMethod())) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Long userId
                    && !rateLimitService.allow("rl:orders:user:" + userId, 60, 10)) {
                writeLimited(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static Long parseScreeningId(String uri) {
        try {
            int start = uri.indexOf(SEATS_PATTERN) + SEATS_PATTERN.length();
            int end = uri.indexOf("/seats", start);
            return Long.valueOf(uri.substring(start, end));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void writeLimited(HttpServletResponse response) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(Result.fail(ResultCode.RATE_LIMITED).toJson());
    }
}
