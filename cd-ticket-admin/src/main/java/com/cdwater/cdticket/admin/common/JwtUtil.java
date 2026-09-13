package com.cdwater.cdticket.admin.common;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String USER_PREFIX = "u";
    private static final String ADMIN_PREFIX = "a";

    private final SecretKey key;
    private final long accessExpireSeconds;

    public JwtUtil(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = properties.getAccessExpireSeconds();
    }

    public String createUserAccessToken(Long userId) {
        return createToken(USER_PREFIX + userId);
    }

    public String createAdminAccessToken(Long adminId) {
        return createToken(ADMIN_PREFIX + adminId);
    }

    private String createToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpireSeconds)))
                .signWith(key)
                .compact();
    }

    /** 解析任意类型 JWT 的 subject；无效/过期抛 JwtException。 */
    public String parseSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long parseUserId(String token) {
        String sub = parseSubject(token);
        if (!sub.startsWith(USER_PREFIX)) {
            throw new JwtException("token type mismatch, expected user");
        }
        return Long.valueOf(sub.substring(1));
    }

    public Long parseAdminId(String token) {
        String sub = parseSubject(token);
        if (!sub.startsWith(ADMIN_PREFIX)) {
            throw new JwtException("token type mismatch, expected admin");
        }
        return Long.valueOf(sub.substring(1));
    }
}
