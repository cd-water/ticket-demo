package com.cdwater.cdticket.app.domain;

import com.cdwater.cdticket.app.infrastructure.entity.User;

public interface UserRepository {
    User findByPhone(String phone);
    User findById(Long id);
    User save(User user);
}