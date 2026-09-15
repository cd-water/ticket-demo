package com.cdwater.cdticket.app.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sms")
@Getter
@Setter
public class SmsProperties {
    private Long codeExpireSeconds;
}