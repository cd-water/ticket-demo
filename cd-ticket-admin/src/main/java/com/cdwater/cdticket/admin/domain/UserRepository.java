package com.cdwater.cdticket.admin.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdwater.cdticket.admin.domain.entity.User;

public interface UserRepository {
    User findByPhone(String phone);
    User findById(Long id);
    User save(User user);
    IPage<User> pageByPhone(IPage<User> page, String phone);
}