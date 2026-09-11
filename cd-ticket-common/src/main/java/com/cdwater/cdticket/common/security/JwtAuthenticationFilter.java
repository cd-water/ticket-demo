package com.cdwater.cdticket.common.security;

import com.cdwater.cdticket.common.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String subject = jwtUtil.parseSubject(token);
                if (subject.startsWith("u")) {
                    Long userId = Long.valueOf(subject.substring(1));
                    setAuthentication(userId);
                } else if (subject.startsWith("a")) {
                    Long adminId = Long.valueOf(subject.substring(1));
                    String stored = redisTemplate.opsForValue().get("admin:token:" + adminId);
                    if (stored != null && stored.equals(token)) {
                        setAuthentication(adminId);
                    }
                }
            } catch (JwtException | IllegalArgumentException ex) {
                // 无效/过期/类型不符 Token → 不设置认证，受保护路径由入口点返回 401
            }
        }
        chain.doFilter(request, response);
    }

    private void setAuthentication(Long id) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(id, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
