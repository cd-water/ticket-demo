package com.cdwater.cdticket.admin.interfaces;

import com.cdwater.cdticket.admin.application.AdminManageService;
import com.cdwater.cdticket.admin.application.dto.AdminCreateCommand;
import com.cdwater.cdticket.admin.application.dto.AdminManageVO;
import com.cdwater.cdticket.admin.application.dto.AdminUpdateCommand;
import com.cdwater.cdticket.admin.interfaces.dto.AdminCreateRequest;
import com.cdwater.cdticket.admin.interfaces.dto.AdminUpdateRequest;
import com.cdwater.cdticket.admin.common.AdminAuthorizer;
import com.cdwater.cdticket.admin.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
public class AdminManageController {

    private final AdminManageService adminManageService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<List<AdminManageVO>> list(@RequestParam(required = false) Integer role) {
        return Result.success(adminManageService.list(role));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid AdminCreateRequest req) {
        adminManageService.create(new AdminCreateCommand(req.getUsername(), req.getPassword(),
                req.getRole(), req.getCinemaId()));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid AdminUpdateRequest req) {
        adminAuthorizer.requireSuperAdmin();
        adminManageService.update(id, new AdminUpdateCommand(req.getUsername(), req.getRole(),
                req.getCinemaId(), req.getStatus()));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireSuperAdmin();
        adminManageService.delete(id);
        return Result.success();
    }
}
