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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenStoreService tokenStore;

    public List<AdminVO> list() {
        return adminMapper.selectList(new LambdaQueryWrapper<Admin>().orderByAsc(Admin::getId))
                .stream().map(AdminService::toVO).toList();
    }

    public void create(AdminSaveRequest req) {
        if (adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, req.getUsername())) != null) {
            throw new BizException("用户名已存在", ResultCode.CONFLICT.getCode());
        }
        Admin admin = new Admin();
        admin.setUsername(req.getUsername());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        adminMapper.insert(admin);
    }

    public void resetPassword(Long id, ResetPasswordRequest req) {
        Admin target = adminMapper.selectById(id);
        if (target == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        target.setPassword(passwordEncoder.encode(req.getPassword()));
        adminMapper.updateById(target);
        tokenStore.revoke(id);
    }

    public void toggleStatus(Long id, int status) {
        if (id.equals(SecurityUtils.getCurrentId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        Admin target = adminMapper.selectById(id);
        if (target == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        target.setStatus(status);
        adminMapper.updateById(target);
        if (status == 0) {
            tokenStore.revoke(id);
        }
    }

    private static AdminVO toVO(Admin a) {
        AdminVO v = new AdminVO();
        v.setId(a.getId());
        v.setUsername(a.getUsername());
        v.setStatus(a.getStatus());
        v.setCreateTime(a.getCreateTime());
        v.setUpdateTime(a.getUpdateTime());
        return v;
    }
}
