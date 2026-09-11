package com.cdwater.cdticket.user.domain;

import com.cdwater.cdticket.user.infrastructure.entity.User;

public interface UserRepository {
    User findByPhone(String phone);
    User findById(Long id);
    User save(User user);
}