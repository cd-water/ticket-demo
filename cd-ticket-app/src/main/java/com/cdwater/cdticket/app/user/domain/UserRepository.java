package com.cdwater.cdticket.app.user.domain;

import com.cdwater.cdticket.app.user.infrastructure.entity.User;

public interface UserRepository {
    User findByPhone(String phone);
    User findById(Long id);
    User save(User user);
}