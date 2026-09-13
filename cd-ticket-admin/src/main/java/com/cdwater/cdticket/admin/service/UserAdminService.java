package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.UserConvert;
import com.cdwater.cdticket.admin.dto.user.UserAdminVO;
import com.cdwater.cdticket.admin.entity.User;
import com.cdwater.cdticket.admin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserMapper userMapper;

    public PageResult<UserAdminVO> page(int page, int size, String phone) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var p = userMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<User>()
                .like(phone != null && !phone.isBlank(), User::getPhone, phone)
                .orderByDesc(User::getId));
        return PageResult.of(p.convert(UserConvert.INSTANCE::toAdminVO));
    }

    public void updateStatus(Long id, int status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
