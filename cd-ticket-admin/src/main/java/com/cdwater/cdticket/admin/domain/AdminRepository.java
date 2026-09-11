package com.cdwater.cdticket.admin.domain;

import com.cdwater.cdticket.admin.infrastructure.entity.Admin;

import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findById(Long id);
}
