package org.xiaoxu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.annotation.OperationLog;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.pojo.SysAnnouncement;
import org.xiaoxu.service.AnnouncementService;

import java.util.List;
import java.util.Map;

/**
 * 系统公告管理
 */
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    /**
     * 分页查询公告（管理员）
     */
    @PreAuthorize("hasAuthority('system:announcement:query')")
    @GetMapping("/list")
    public Result<IPage<SysAnnouncement>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        return Result.success(announcementService.pageQuery(current, size, title, status));
    }

    /**
     * 获取当前用户未读的公告（登录后弹窗展示）
     */
    @GetMapping("/unread")
    public Result<List<SysAnnouncement>> unread(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(announcementService.getUnread(userId));
    }

    /**
     * 标记公告为已读
     */
    @PostMapping("/read")
    public Result<?> markRead(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Object idObj = body.get("id");
        if (idObj == null) {
            // 批量已读
            List<?> ids = (List<?>) body.get("ids");
            if (ids != null) {
                List<Long> announcementIds = ids.stream()
                        .map(i -> Long.valueOf(i.toString()))
                        .toList();
                announcementService.markAllRead(announcementIds, userId);
            }
        } else {
            announcementService.markRead(Long.valueOf(idObj.toString()), userId);
        }
        return Result.success();
    }

    @OperationLog(module = "公告管理", operation = "新增公告")
    @PreAuthorize("hasAuthority('system:announcement:create')")
    @PostMapping("/create")
    public Result<?> create(@RequestBody SysAnnouncement announcement) {
        announcementService.create(announcement);
        return Result.success();
    }

    @OperationLog(module = "公告管理", operation = "编辑公告")
    @PreAuthorize("hasAuthority('system:announcement:update')")
    @PutMapping("/update")
    public Result<?> update(@RequestBody SysAnnouncement announcement) {
        announcementService.update(announcement);
        return Result.success();
    }

    @OperationLog(module = "公告管理", operation = "发布公告")
    @PreAuthorize("hasAuthority('system:announcement:update')")
    @PutMapping("/publish/{id}")
    public Result<?> publish(@PathVariable Long id) {
        String publisher = SecurityContextHolder.getContext().getAuthentication().getName();
        announcementService.publish(id, publisher);
        return Result.success();
    }

    @OperationLog(module = "公告管理", operation = "撤回公告")
    @PreAuthorize("hasAuthority('system:announcement:update')")
    @PutMapping("/withdraw/{id}")
    public Result<?> withdraw(@PathVariable Long id) {
        announcementService.withdraw(id);
        return Result.success();
    }

    @OperationLog(module = "公告管理", operation = "删除公告")
    @PreAuthorize("hasAuthority('system:announcement:delete')")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
