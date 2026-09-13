package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminManageService {

    private final AdminMapper adminMapper;
    private final CinemaAdminService cinemaService;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuthorizer adminAuthorizer;
    private final TokenStoreService tokenStore;

    /** 列表（仅平台管理员） */
    public List<AdminManageVO> list(Integer role) {
        adminAuthorizer.requirePlatformAdmin();
        return adminMapper.selectListWithCinema(role, null);
    }

    /** 新增/修改（仅平台管理员） */
    public void save(AdminSaveRequest req) {
        adminAuthorizer.requirePlatformAdmin();
        boolean isCreate = req.getId() == null;

        // 权限校验
        if (!isCreate && req.getId().equals(adminAuthorizer.currentAdmin().getId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }

        // 角色+影院绑定校验
        if (req.getRole() != null && req.getRole() == 1 && req.getCinemaId() == null) {
            throw new BizException(ResultCode.CINEMA_ADMIN_NEED_CINEMA);
        }
        if (req.getCinemaId() != null && req.getCinemaId() != 0
                && cinemaService.getCinema(req.getCinemaId()) == null) {
            throw new BizException("绑定的影院不存在", ResultCode.CINEMA_ADMIN_NEED_CINEMA.getCode());
        }

        // 用户名唯一校验
        Admin existingAdmin = findByUsername(req.getUsername());
        if (existingAdmin != null && (isCreate || !existingAdmin.getId().equals(req.getId()))) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }

        if (isCreate) {
            Admin admin = new Admin();
            admin.setUsername(req.getUsername());
            admin.setPassword(passwordEncoder.encode(req.getPassword()));
            admin.setRole(req.getRole());
            admin.setCinemaId(req.getRole() != null && req.getRole() == 1 ? req.getCinemaId() : 0L);
            admin.setStatus(1);
            adminMapper.insert(admin);
        } else {
            Admin target = requireAdmin(req.getId());
            target.setUsername(req.getUsername());
            target.setRole(req.getRole());
            target.setCinemaId(req.getRole() != null && req.getRole() == 1 ? req.getCinemaId() : 0L);
            if (req.getStatus() != null) {
                target.setStatus(req.getStatus());
                if (req.getStatus() == 0) {
                    tokenStore.revoke(req.getId());
                }
            }
            adminMapper.updateById(target);
        }
    }

    /** 删除（仅平台管理员） */
    public void delete(Long id) {
        adminAuthorizer.requirePlatformAdmin();
        if (id.equals(adminAuthorizer.currentAdmin().getId())) {
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
