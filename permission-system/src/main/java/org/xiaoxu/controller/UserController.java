package org.xiaoxu.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.common.utils.TokenProvider;
import org.xiaoxu.file.UserExcelVO;
import org.xiaoxu.mapper.UserRoleMapper;
import org.xiaoxu.pojo.SystemUserRole;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.request.UserRoleRequest;
import org.xiaoxu.service.SysUserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private TokenProvider tokenProvider;

    @PreAuthorize("hasAuthority('system:user:create')")
    @PostMapping("/create")
    public Result<?> createUser(@RequestBody SystemUsers user) {
        sysUserService.createUser(user);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:user:delete')")
    @DeleteMapping("/delete")
    public Result<?> deleteUser(@RequestParam Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/getRoleIds")
    public Result<?> getRoleIdsByUserId(@RequestParam Long userId) {
        Set<Long> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
        return Result.success(roleIds);
    }

    @PreAuthorize("hasAuthority('system:user:update')")
    @PutMapping("/updateRole")
    public Result<?> updateUserRole(@RequestBody UserRoleRequest request) {
        // 先删旧关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<SystemUserRole>()
                        .eq(SystemUserRole::getUserId, request.getUserId())
        );
        // 插入新关联
        if (request.getRoleIds() != null) {
            for (Long roleId : request.getRoleIds()) {
                SystemUserRole ur = new SystemUserRole();
                ur.setUserId(request.getUserId());
                ur.setRoleId(roleId);
                ur.setDeleted("0");
                userRoleMapper.insert(ur);
            }
        }
        // 清除该用户的 token，强制重新登录获取新权限
        tokenProvider.removeTokenByUserId(request.getUserId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/export")
    public void exportUserList(HttpServletResponse response) throws Exception {
        List<SystemUsers> users = sysUserService.getAll();
        List<UserExcelVO> voList = (users == null ? List.<SystemUsers>of() : users).stream().map(u -> {
            UserExcelVO vo = new UserExcelVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setEmail(u.getEmail());
            vo.setMobile(u.getMobile());
            vo.setStatus(u.getStatus() == 0 ? "正常" : "禁用");
            vo.setCreateTime(u.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户列表", StandardCharsets.UTF_8).replace("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), UserExcelVO.class).sheet("用户列表").doWrite(voList);
    }
}
