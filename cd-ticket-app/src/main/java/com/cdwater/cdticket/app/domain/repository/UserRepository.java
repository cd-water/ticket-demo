package com.cdwater.cdticket.app.domain.repository;

import com.cdwater.cdticket.app.domain.model.User;

public interface UserRepository {
    User findByPhone(String phone);
    User findById(Long id);
    User save(User user);
}