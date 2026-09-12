package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import com.cdwater.cdticket.admin.infrastructure.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<Admin> list(Integer role, Long cinemaId) {
        return adminMapper.selectList(new LambdaQueryWrapper<Admin>()
                .eq(role != null, Admin::getRole, role)
                .eq(cinemaId != null, Admin::getCinemaId, cinemaId)
                .orderByAsc(Admin::getId));
    }

    @Override
    public Admin save(Admin admin) {
        if (admin.getId() == null) {
            adminMapper.insert(admin);
        } else {
            adminMapper.updateById(admin);
        }
        return admin;
    }

    @Override
    public void deleteById(Long id) {
        adminMapper.deleteById(id);
    }
}