package com.cdwater.cdticket.user.domain;

import com.cdwater.cdticket.user.infrastructure.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByPhone(String phone);
    Optional<User> findById(Long id);
    User save(User user);
}
