package com.cdwater.cdticket.admin.domain;

import com.cdwater.cdticket.admin.infrastructure.entity.Admin;

import java.util.List;

public interface AdminRepository {
    Admin findByUsername(String username);
    Admin findById(Long id);
    List<Admin> list(Integer role, Long cinemaId);
    Admin save(Admin admin);
    void deleteById(Long id);
}