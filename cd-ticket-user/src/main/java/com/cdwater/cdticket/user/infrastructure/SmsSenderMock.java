package com.cdwater.cdticket.user.infrastructure;

import com.cdwater.cdticket.user.application.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsSenderMock implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(SmsSenderMock.class);

    @Override
    public void send(String phone, String code) {
        log.info("[MOCK-SMS] to={} code={}", phone, code);
    }
}
