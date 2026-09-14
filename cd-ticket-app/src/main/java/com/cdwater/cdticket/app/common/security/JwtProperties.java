package com.cdwater.cdticket.app.common.security;

import com.cdwater.cdticket.app.common.ResultCode;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Long accessExpireSeconds;
    private Long refreshExpireSeconds;
}
