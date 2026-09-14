package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.common.exception.GlobalExceptionHandler;
import com.cdwater.cdticket.admin.dto.admin.AdminVO;
import com.cdwater.cdticket.admin.dto.admin.LoginResponse;
import com.cdwater.cdticket.admin.dto.admin.ResetPasswordRequest;
import com.cdwater.cdticket.admin.service.AdminService;
import com.cdwater.cdticket.admin.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private AdminService adminService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        AdminController controller = new AdminController(authService, adminService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_success_returnsEnvelope() throws Exception {
        LoginResponse resp = new LoginResponse();
        resp.setToken("tok-1");
        LoginResponse.AdminInfo info = new LoginResponse.AdminInfo();
        info.setId(1L);
        info.setUsername("admin01");
        resp.setAdmin(info);
        when(authService.login("admin01", "abc12345")).thenReturn(resp);

        mvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin01\",\"password\":\"abc12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("tok-1"))
                .andExpect(jsonPath("$.data.admin.id").value(1));
    }

    @Test
    void login_invalidBody_returns400AndSkipsService() throws Exception {
        mvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"password\":\"abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    void logout_delegates() throws Exception {
        mvc.perform(post("/api/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).logout();
    }

    @Test
    void list_returnsAdminVOList() throws Exception {
        AdminVO vo = new AdminVO();
        vo.setId(1L);
        vo.setUsername("admin01");
        vo.setStatus(1);
        when(adminService.list()).thenReturn(List.of(vo));

        mvc.perform(get("/api/admin/admins/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("admin01"));
    }

    @Test
    void create_validBody_delegates() throws Exception {
        mvc.perform(post("/api/admin/admins/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin02\",\"password\":\"abc12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).create(any());
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/admin/admins/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verify(adminService, never()).create(any());
    }

    @Test
    void resetPassword_validBody_delegates() throws Exception {
        mvc.perform(post("/api/admin/admins/5/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newpass123\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ResetPasswordRequest> captor = ArgumentCaptor.forClass(ResetPasswordRequest.class);
        verify(adminService).resetPassword(org.mockito.ArgumentMatchers.eq(5L), captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("newpass123");
    }

    @Test
    void resetPassword_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/admin/admins/5/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"short\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verify(adminService, never()).resetPassword(org.mockito.ArgumentMatchers.anyLong(), any());
    }

    @Test
    void toggleStatus_delegates() throws Exception {
        mvc.perform(post("/api/admin/admins/3/status").param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminService).toggleStatus(3L, 0);
    }
}
