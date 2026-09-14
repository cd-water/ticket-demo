package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    private AdminMapper adminMapper;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminMapper = mock(AdminMapper.class);
        adminService = new AdminService(adminMapper,
                mock(PasswordEncoder.class), mock(TokenStoreService.class));
    }

    /** 并发创建同名管理员撞唯一键：service 不吞异常，交由全局异常处理兜底（避免 500） */
    @Test
    void createLetsDuplicateKeyPropagate() {
        when(adminMapper.insert(any(Admin.class))).thenThrow(new DuplicateKeyException("uk_username"));

        AdminSaveRequest req = new AdminSaveRequest();
        req.setUsername("adminx");
        req.setPassword("Aa123456");

        assertThrows(DuplicateKeyException.class, () -> adminService.create(req));
    }
}
