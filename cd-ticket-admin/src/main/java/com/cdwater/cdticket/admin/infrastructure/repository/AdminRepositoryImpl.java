package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import com.cdwater.cdticket.admin.infrastructure.mapper.AdminMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminMapper adminMapper;

    public AdminRepositoryImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return Optional.ofNullable(adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)));
    }

    @Override
    public Optional<Admin> findById(Long id) {
        return Optional.ofNullable(adminMapper.selectById(id));
    }
}
