package com.cdwater.cdticket.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cdwater.cdticket.admin")
@MapperScan("com.cdwater.cdticket.admin.infrastructure.mapper")
public class CdTicketAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(CdTicketAdminApplication.class, args);
    }
}
