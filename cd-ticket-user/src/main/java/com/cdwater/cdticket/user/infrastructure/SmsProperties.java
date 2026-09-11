package com.cdwater.cdticket.user.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {
    private long codeExpireSeconds;
    private long sendCooldownSeconds;

    public long getCodeExpireSeconds() { return codeExpireSeconds; }
    public void setCodeExpireSeconds(long codeExpireSeconds) { this.codeExpireSeconds = codeExpireSeconds; }
    public long getSendCooldownSeconds() { return sendCooldownSeconds; }
    public void setSendCooldownSeconds(long sendCooldownSeconds) { this.sendCooldownSeconds = sendCooldownSeconds; }
}
