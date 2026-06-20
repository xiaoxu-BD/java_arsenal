package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.pojo.SystemMenu;
import org.xiaoxu.service.MenuService;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    /**
     * 当前用户可见的菜单树（侧边栏用，登录用户都能看自己的菜单）
     */
    @GetMapping("/list")
    public Result<?> getUserMenuTree(HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) {
            return Result.error(401, "获取用户信息失败");
        }
        return Result.success(menuService.getUserMenuTree(userId));
    }

    /**
     * 全部菜单树（菜单管理页面用，不过滤权限）
     */
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/listAll")
    public Result<?> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }

    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/get")
    public Result<?> getMenuById(@RequestParam Long id) {
        return Result.success(menuService.getMenuById(id));
    }

    @PreAuthorize("hasAuthority('system:menu:create')")
    @PostMapping("/create")
    public Result<?> createMenu(@RequestBody SystemMenu menu) {
        menuService.createMenu(menu);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:menu:update')")
    @PutMapping("/update")
    public Result<?> updateMenu(@RequestBody SystemMenu menu) {
        menuService.updateMenu(menu);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:menu:delete')")
    @DeleteMapping("/delete")
    public Result<?> deleteMenu(@RequestParam Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }

    private Long extractUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
    }
}
