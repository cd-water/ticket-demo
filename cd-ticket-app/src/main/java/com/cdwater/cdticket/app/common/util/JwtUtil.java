package com.cdwater.cdticket.app.common.util;

import com.cdwater.cdticket.app.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpireSeconds;

    public JwtUtil(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = properties.getAccessExpireSeconds();
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpireSeconds)))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 JWT 的 subject；无效/过期抛 JwtException。
     */
    public String parseSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long parseUserId(String token) {
        return Long.valueOf(parseSubject(token));
    }
}
