package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.AdminPrincipal;
import com.cdwater.cdticket.admin.application.exception.BizException;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAuthorizerImplTest {

    private AdminRepository adminRepository;
    private AdminAuthorizerImpl authorizer;

    @BeforeEach
    void setUp() {
        adminRepository = mock(AdminRepository.class);
        authorizer = new AdminAuthorizerImpl(adminRepository);
    }

    private Admin admin(long id, int role, long cinemaId, int status) {
        Admin a = new Admin();
        a.setId(id);
        a.setUsername("admin" + id);
        a.setRole(role);
        a.setCinemaId(cinemaId);
        a.setStatus(status);
        return a;
    }

    /** currentAdmin 依赖 SecurityContext；单测通过受保护方法提取逻辑不便，改为直接测三个 require 方法（内部走 currentAdmin 需 mock 静态，见说明） */
    @Test
    void superAdminPassesRequireSuperAdmin() {
        // currentAdmin 内部调用 SecurityUtils.getCurrentId()（静态），单测改为子类覆写 currentAdmin
        AuthorizerWithStubCurrent stub = new AuthorizerWithStubCurrent(admin(1L, 0, 0L, 1));
        assertDoesNotThrow(stub::requireSuperAdmin);
    }

    @Test
    void cinemaAdminRejectedByRequireSuperAdmin() {
        AuthorizerWithStubCurrent stub = new AuthorizerWithStubCurrent(admin(2L, 1, 5L, 1));
        assertThrows(BizException.class, stub::requireSuperAdmin);
    }

    @Test
    void scopeMismatchRejected() {
        AuthorizerWithStubCurrent stub = new AuthorizerWithStubCurrent(admin(2L, 1, 5L, 1));
        assertThrows(BizException.class, () -> stub.requireScope(9L));
        assertDoesNotThrow(() -> stub.requireScope(5L));
    }

    /** 覆写 currentAdmin 以绕过 SecurityContext 静态调用（禁用拒绝在 resolveAdmin 内，集成冒烟覆盖） */
    static class AuthorizerWithStubCurrent extends AdminAuthorizerImpl {
        private final AdminPrincipal current;

        AuthorizerWithStubCurrent(Admin admin) {
            super(null);
            this.current = new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getRole(), admin.getCinemaId());
        }

        @Override
        public AdminPrincipal currentAdmin() {
            return current;
        }
    }
}
