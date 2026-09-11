package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import com.cdwater.cdticket.admin.infrastructure.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminMapper adminMapper;

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
    }

    @Override
    public Admin findById(Long id) {
        return adminMapper.selectById(id);
    }
}