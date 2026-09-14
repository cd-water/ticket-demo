package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.user.UserVO;
import com.cdwater.cdticket.admin.entity.User;
import com.cdwater.cdticket.admin.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    void page_returnsConvertedPage() {
        User u = new User();
        u.setId(1L);
        u.setPhone("13800000000");
        u.setStatus(1);
        Page<User> userPage = new Page<>(1, 10);
        userPage.setTotal(2);
        userPage.setRecords(List.of(u));
        when(userMapper.selectPage(any(), any())).thenReturn(userPage);

        PageResult<UserVO> pr = userService.page(1, 10, "138", 1);

        assertThat(pr.getTotal()).isEqualTo(2);
        assertThat(pr.getRecords()).hasSize(1);
        assertThat(pr.getRecords().get(0).getPhone()).isEqualTo("13800000000");
    }

    @Test
    void toggleStatus_success_updates() {
        User u = new User();
        u.setId(9L);
        u.setStatus(1);
        when(userMapper.selectById(9L)).thenReturn(u);

        userService.toggleStatus(9L, 0);

        assertThat(u.getStatus()).isZero();
        verify(userMapper).updateById(u);
    }

    @Test
    void toggleStatus_notFound_throws404() {
        when(userMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> userService.toggleStatus(9L, 0))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
        verify(userMapper, never()).updateById(any(User.class));
    }
}
