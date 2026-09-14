package com.cdwater.cdticket.admin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Token 配置项
 */
@Component
@ConfigurationProperties(prefix = "token")
@Getter
@Setter
public class TokenProperties {

    private Long expireSeconds;
}
