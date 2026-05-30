package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.pojo.SystemRole;
import org.xiaoxu.pojo.request.RoleMenuRequest;
import org.xiaoxu.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/list")
    public Result<?> getRoleList() {
        return Result.success(roleService.getRoleList());
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/get")
    public Result<?> getRoleDetail(@RequestParam Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }

    @PreAuthorize("hasAuthority('system:role:create')")
    @PostMapping("/create")
    public Result<?> createRole(@RequestBody SystemRole role) {
        roleService.createRole(role);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:role:update')")
    @PutMapping("/update")
    public Result<?> updateRole(@RequestBody SystemRole role) {
        roleService.updateRole(role);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:role:delete')")
    @DeleteMapping("/delete")
    public Result<?> deleteRole(@RequestParam Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:role:update')")
    @PutMapping("/updateMenu")
    public Result<?> updateRoleMenu(@RequestBody RoleMenuRequest request) {
        roleService.updateRoleMenu(request.getRoleId(), request.getMenuIds());
        return Result.success();
    }

}
