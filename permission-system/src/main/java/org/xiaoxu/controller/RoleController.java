package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.xiaoxu.annotation.OperationLog;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.constants.PermissionConstants;
import org.xiaoxu.common.utils.ExcelExportUtil;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.file.RoleExcelVO;
import org.xiaoxu.pojo.SystemRole;
import org.xiaoxu.pojo.request.RoleMenuRequest;
import org.xiaoxu.service.RoleService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_QUERY + ")")
    @GetMapping("/list")
    public Result<?> getRoleList() {
        return Result.success(roleService.getRoleList());
    }

    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_QUERY + ")")
    @GetMapping("/get")
    public Result<?> getRoleDetail(@RequestParam Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }

    @OperationLog(module = "角色管理", operation = "新增角色")
    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_CREATE + ")")
    @PostMapping("/create")
    public Result<?> createRole(@RequestBody SystemRole role) {
        roleService.createRole(role);
        return Result.success();
    }

    @OperationLog(module = "角色管理", operation = "修改角色")
    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_UPDATE + ")")
    @PutMapping("/update")
    public Result<?> updateRole(@RequestBody SystemRole role) {
        roleService.updateRole(role);
        return Result.success();
    }

    @OperationLog(module = "角色管理", operation = "删除角色")
    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_DELETE + ")")
    @DeleteMapping("/delete")
    public Result<?> deleteRole(@RequestParam Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_UPDATE + ")")
    @PutMapping("/updateMenu")
    public Result<?> updateRoleMenu(@RequestBody RoleMenuRequest request) {
        roleService.updateRoleMenu(request.getRoleId(), request.getMenuIds());
        return Result.success();
    }

    @OperationLog(module = "角色管理", operation = "导出角色")
    @PreAuthorize("hasAuthority(" + PermissionConstants.ROLE_QUERY + ")")
    @GetMapping("/export")
    public void exportRoleList(HttpServletResponse response) throws Exception {
        List<SystemRole> roles = roleService.getRoleList();

        Map<Long, String> scopeMap = new LinkedHashMap<>();
        scopeMap.put(1L, "全部数据权限");
        scopeMap.put(2L, "本部门数据权限");
        scopeMap.put(5L, "仅本人数据权限");

        List<RoleExcelVO> voList = roles.stream().map(r -> {
            RoleExcelVO vo = new RoleExcelVO();
            vo.setId(r.getId());
            vo.setName(r.getName());
            vo.setCode(r.getCode());
            vo.setSort(r.getSort());
            vo.setDataScope(scopeMap.getOrDefault(r.getDataScope(), String.valueOf(r.getDataScope())));
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        ExcelExportUtil.write(response, "角色列表", "角色列表", RoleExcelVO.class, voList);
    }

}
