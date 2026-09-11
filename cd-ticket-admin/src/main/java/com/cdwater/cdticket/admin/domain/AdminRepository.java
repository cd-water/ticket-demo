package com.cdwater.cdticket.admin.domain;

import com.cdwater.cdticket.admin.infrastructure.entity.Admin;

public interface AdminRepository {
    Admin findByUsername(String username);
    Admin findById(Long id);
}