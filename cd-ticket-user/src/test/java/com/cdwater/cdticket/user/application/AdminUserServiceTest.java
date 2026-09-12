package com.cdwater.cdticket.user.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.user.application.dto.UserAdminVO;
import com.cdwater.cdticket.user.domain.UserRepository;
import com.cdwater.cdticket.user.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserServiceTest {

    private UserRepository repo;
    private AdminUserService service;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        service = new AdminUserService(repo);
    }

    @Test
    void pageFiltersByPhone() {
        Page<User> p = new Page<>(1, 10, 1);
        p.setRecords(List.of(user()));
        when(repo.pageByPhone(any(), eq("138"))).thenReturn(p);

        PageResult<UserAdminVO> r = service.page(1, 10, "138");

        assertEquals(1, r.getRecords().size());
        verify(repo).pageByPhone(any(), eq("138"));
    }

    @Test
    void updateStatusMissingThrowsNotFound() {
        when(repo.findById(9L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.updateStatus(9L, 0));
    }

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setPhone("13800138000");
        u.setNickname("用户8000");
        u.setStatus(1);
        return u;
    }
}
