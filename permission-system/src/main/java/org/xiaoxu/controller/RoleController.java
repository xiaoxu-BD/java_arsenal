package org.xiaoxu.controller;

import com.alibaba.excel.EasyExcel;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.file.RoleExcelVO;
import org.xiaoxu.pojo.SystemRole;
import org.xiaoxu.pojo.request.RoleMenuRequest;
import org.xiaoxu.service.RoleService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PreAuthorize("hasAuthority('system:role:query')")
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

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("角色列表", StandardCharsets.UTF_8).replace("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), RoleExcelVO.class).sheet("角色列表").doWrite(voList);
    }

}
