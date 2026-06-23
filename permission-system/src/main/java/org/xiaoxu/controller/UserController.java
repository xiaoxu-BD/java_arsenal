package org.xiaoxu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.annotation.OperationLog;
import org.xiaoxu.common.utils.ExcelExportUtil;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.common.utils.TokenProvider;
import org.xiaoxu.file.UserExcelVO;
import org.xiaoxu.mapper.UserRoleMapper;
import org.xiaoxu.pojo.SystemUserRole;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.request.ChangePasswordRequest;
import org.xiaoxu.pojo.request.ResetPasswordRequest;
import org.xiaoxu.pojo.request.UserRoleRequest;
import org.xiaoxu.service.OssService;
import org.xiaoxu.service.SysUserService;

import java.util.List;
import java.util.Map;
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

    @Resource
    private OssService ossService;

    @OperationLog(module = "用户管理", operation = "新增用户")
    @PreAuthorize("hasAuthority('system:user:create')")
    @PostMapping("/create")
    public Result<?> createUser(@RequestBody SystemUsers user) {
        sysUserService.createUser(user);
        return Result.success();
    }

    @OperationLog(module = "用户管理", operation = "删除用户")
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

    @OperationLog(module = "用户管理", operation = "导出用户")
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

        ExcelExportUtil.write(response, "用户列表", "用户列表", UserExcelVO.class, voList);
    }

    /**
     * 获取当前登录用户完整信息
     */
    @GetMapping("/profile")
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SystemUsers user = sysUserService.getUserById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    /**
     * 修改个人资料（昵称、邮箱、手机、性别）
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody SystemUsers update, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        update.setId(userId);
        sysUserService.updateProfile(update);
        return Result.success();
    }

    /**
     * 修改自己的密码（需要旧密码）
     */
    @PutMapping("/changePassword")
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            sysUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 管理员重置指定用户密码
     */
    @PreAuthorize("hasAuthority('system:user:update')")
    @PutMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            sysUserService.resetPassword(request.getUserId(), request.getNewPassword());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String url = ossService.uploadFile(file, "avatar");
        sysUserService.updateAvatar(userId, url);
        return Result.success(url);
    }
}
