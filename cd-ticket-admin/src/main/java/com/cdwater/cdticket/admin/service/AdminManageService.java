package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.AdminConvert;
import com.cdwater.cdticket.admin.dto.admin.AdminCreateRequest;
import com.cdwater.cdticket.admin.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.dto.admin.AdminUpdateRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.AdminAuthorizer;
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

    /** 列表（不分页）；影院管理员只列本影院 */
    public List<AdminManageVO> list(Integer role) {
        var current = adminAuthorizer.currentAdmin();
        Long cinemaId = current.getRole() == 1 ? current.getCinemaId() : null;
        return adminMapper.selectList(new LambdaQueryWrapper<Admin>()
                .eq(role != null, Admin::getRole, role)
                .eq(cinemaId != null, Admin::getCinemaId, cinemaId)
                .orderByAsc(Admin::getId)).stream().map(AdminConvert.INSTANCE::toManageVO).toList();
    }

    /** 新增；影院管理员仅能新增本影院、role=1 的管理员 */
    public void create(AdminCreateRequest req) {
        var current = adminAuthorizer.currentAdmin();
        if (current.getRole() == 1) {
            if (req.getRole() == null || req.getRole() != 1) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
            if (req.getCinemaId() == null || !req.getCinemaId().equals(current.getCinemaId())) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
        } else {
            if (req.getRole() != null && req.getRole() == 1 && req.getCinemaId() == null) {
                throw new BizException(ResultCode.CINEMA_ADMIN_NEED_CINEMA);
            }
            if (req.getCinemaId() != null && req.getCinemaId() != 0
                    && cinemaService.getCinema(req.getCinemaId()) == null) {
                throw new BizException("绑定的影院不存在", ResultCode.CINEMA_ADMIN_NEED_CINEMA.getCode());
            }
        }
        if (findByUsername(req.getUsername()) != null) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }
        Admin admin = new Admin();
        admin.setUsername(req.getUsername());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setRole(req.getRole() == null ? 0 : req.getRole());
        admin.setCinemaId(req.getRole() != null && req.getRole() == 1
                ? req.getCinemaId() : 0L);
        admin.setStatus(1);
        adminMapper.insert(admin);
    }

    /** 修改（超管专属，Controller 已 requireSuperAdmin） */
    public void update(Long id, AdminUpdateRequest req) {
        var current = adminAuthorizer.currentAdmin();
        if (id.equals(current.getId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        Admin target = requireAdmin(id);
        if (!target.getUsername().equals(req.getUsername())
                && findByUsername(req.getUsername()) != null) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }
        if (req.getRole() != null && req.getRole() == 1 && req.getCinemaId() == null) {
            throw new BizException(ResultCode.CINEMA_ADMIN_NEED_CINEMA);
        }
        if (req.getRole() != null && req.getRole() == 1
                && cinemaService.getCinema(req.getCinemaId()) == null) {
            throw new BizException("绑定的影院不存在", ResultCode.CINEMA_ADMIN_NEED_CINEMA.getCode());
        }
        target.setUsername(req.getUsername());
        target.setRole(req.getRole());
        target.setCinemaId(req.getRole() == 1 ? req.getCinemaId() : 0L);
        target.setStatus(req.getStatus());
        adminMapper.updateById(target);
    }

    /** 逻辑删除 + 用户名改写（避开 uk_username，同名可重建）；超管专属 */
    public void delete(Long id) {
        var current = adminAuthorizer.currentAdmin();
        if (id.equals(current.getId())) {
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
