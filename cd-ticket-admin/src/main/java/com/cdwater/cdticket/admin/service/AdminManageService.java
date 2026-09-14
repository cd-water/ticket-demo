package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.AdminVO;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.dto.admin.ResetPasswordRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminManageService {
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenStoreService tokenStore;

    public List<AdminVO> list() {
        return adminMapper.selectListAll();
    }

    public void create(AdminSaveRequest req) {
        if (findByUsername(req.getUsername()) != null) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }
        Admin admin = new Admin();
        admin.setUsername(req.getUsername());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setStatus(1);
        adminMapper.insert(admin);
    }

    public void resetPassword(Long id, ResetPasswordRequest req) {
        Admin target = requireAdmin(id);
        target.setPassword(passwordEncoder.encode(req.getPassword()));
        adminMapper.updateById(target);
        tokenStore.revoke(id);
    }

    public void toggleStatus(Long id, int status) {
        if (id.equals(SecurityUtils.getCurrentId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        Admin target = requireAdmin(id);
        target.setStatus(status);
        adminMapper.updateById(target);
        if (status == 0) {
            tokenStore.revoke(id);
        }
    }

    @Transactional
    public void delete(Long id) {
        if (id.equals(SecurityUtils.getCurrentId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        Admin target = requireAdmin(id);
        target.setUsername(target.getUsername() + "_del_" + id);
        adminMapper.updateById(target);
        adminMapper.deleteById(id);
    }

    private Admin findByUsername(String username) {
        return adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
    }

    private Admin requireAdmin(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return admin;
    }
}
