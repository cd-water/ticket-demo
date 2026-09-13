package com.cdwater.cdticket.admin.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.application.dto.UserAdminVO;
import com.cdwater.cdticket.admin.common.api.PageResult;
import com.cdwater.cdticket.admin.common.api.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.domain.UserRepository;
import com.cdwater.cdticket.admin.domain.entity.User;
import com.cdwater.cdticket.admin.infrastructure.convert.UserConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    public PageResult<UserAdminVO> page(int page, int size, String phone) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var p = userRepository.pageByPhone(Page.of(page, size), phone);
        return PageResult.of(p.convert(UserConvert.INSTANCE::toAdminVO));
    }

    public void updateStatus(Long id, int status) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        user.setStatus(status);
        userRepository.save(user);
    }
}