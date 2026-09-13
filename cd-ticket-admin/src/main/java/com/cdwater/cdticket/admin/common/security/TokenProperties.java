package com.cdwater.cdticket.admin.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "token")
@Getter
@Setter
public class TokenProperties {
    /** token 有效期（秒）；每次请求滑动续期 */
    private Long expireSeconds;
}
