package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.CinemaAdminService;
import com.cdwater.cdticket.admin.application.dto.AdminCreateCommand;
import com.cdwater.cdticket.admin.application.dto.AdminManageVO;
import com.cdwater.cdticket.admin.application.dto.AdminUpdateCommand;
import com.cdwater.cdticket.admin.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.admin.common.api.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.domain.entity.Admin;
import com.cdwater.cdticket.admin.infrastructure.convert.AdminConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminManageService {

    private final AdminRepository adminRepository;
    private final CinemaAdminService cinemaService;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuthorizer adminAuthorizer;

    /** 列表（不分页）；影院管理员只列本影院 */
    public List<AdminManageVO> list(Integer role) {
        var current = adminAuthorizer.currentAdmin();
        Long cinemaId = current.getRole() == 1 ? current.getCinemaId() : null;
        return adminRepository.list(role, cinemaId).stream().map(AdminConvert.INSTANCE::toManageVO).toList();
    }

    /** 新增；影院管理员仅能新增本影院、role=1 的管理员 */
    public void create(AdminCreateCommand command) {
        var current = adminAuthorizer.currentAdmin();
        if (current.getRole() == 1) {
            if (command.getRole() == null || command.getRole() != 1) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
            if (command.getCinemaId() == null || !command.getCinemaId().equals(current.getCinemaId())) {
                throw new BizException(ResultCode.FORBIDDEN);
            }
        } else {
            if (command.getRole() != null && command.getRole() == 1 && command.getCinemaId() == null) {
                throw new BizException(ResultCode.CINEMA_ADMIN_NEED_CINEMA);
            }
            if (command.getCinemaId() != null && command.getCinemaId() != 0
                    && cinemaService.getCinema(command.getCinemaId()) == null) {
                throw new BizException("绑定的影院不存在", ResultCode.CINEMA_ADMIN_NEED_CINEMA.getCode());
            }
        }
        if (adminRepository.findByUsername(command.getUsername()) != null) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }
        Admin admin = new Admin();
        admin.setUsername(command.getUsername());
        admin.setPassword(passwordEncoder.encode(command.getPassword()));
        admin.setRole(command.getRole() == null ? 0 : command.getRole());
        admin.setCinemaId(command.getRole() != null && command.getRole() == 1
                ? command.getCinemaId() : 0L);
        admin.setStatus(1);
        adminRepository.save(admin);
    }

    /** 修改（超管专属，Controller 已 requireSuperAdmin） */
    public void update(Long id, AdminUpdateCommand command) {
        var current = adminAuthorizer.currentAdmin();
        if (id.equals(current.getId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        Admin target = requireAdmin(id);
        if (!target.getUsername().equals(command.getUsername())
                && adminRepository.findByUsername(command.getUsername()) != null) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }
        if (command.getRole() != null && command.getRole() == 1 && command.getCinemaId() == null) {
            throw new BizException(ResultCode.CINEMA_ADMIN_NEED_CINEMA);
        }
        if (command.getRole() != null && command.getRole() == 1
                && cinemaService.getCinema(command.getCinemaId()) == null) {
            throw new BizException("绑定的影院不存在", ResultCode.CINEMA_ADMIN_NEED_CINEMA.getCode());
        }
        target.setUsername(command.getUsername());
        target.setRole(command.getRole());
        target.setCinemaId(command.getRole() == 1 ? command.getCinemaId() : 0L);
        target.setStatus(command.getStatus());
        adminRepository.save(target);
    }

    /** 逻辑删除 + 用户名改写（避开 uk_username，同名可重建）；超管专属 */
    public void delete(Long id) {
        var current = adminAuthorizer.currentAdmin();
        if (id.equals(current.getId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        Admin target = requireAdmin(id);
        target.setUsername(target.getUsername() + "_del_" + id);
        adminRepository.save(target);
        adminRepository.deleteById(id);
    }

    private Admin requireAdmin(Long id) {
        Admin admin = adminRepository.findById(id);
        if (admin == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return admin;
    }
}
