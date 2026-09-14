package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.user.UserVO;
import com.cdwater.cdticket.admin.entity.User;
import com.cdwater.cdticket.admin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public PageResult<UserVO> page(int page, int size, String phone, Integer status) {
        IPage<User> p = userMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<User>()
                .like(phone != null && !phone.isBlank(), User::getPhone, phone)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getId));
        return PageResult.of(p.convert(UserService::toVO));
    }

    public void toggleStatus(Long id, int status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private static UserVO toVO(User u) {
        UserVO v = new UserVO();
        v.setId(u.getId());
        v.setPhone(u.getPhone());
        v.setNickname(u.getNickname());
        v.setStatus(u.getStatus());
        v.setCreateTime(u.getCreateTime());
        v.setUpdateTime(u.getUpdateTime());
        return v;
    }
}
