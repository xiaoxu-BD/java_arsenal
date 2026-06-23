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
import org.xiaoxu.pojo.request.MarkReadRequest;
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
     * 公告详情
     */
    @PreAuthorize("hasAuthority('system:announcement:query')")
    @GetMapping("/detail/{id}")
    public Result<SysAnnouncement> detail(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
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
     * 获取最新的已发布公告（带已读标记，首页用）
     * @param limit 获取条数，默认 3
     */
    @GetMapping("/latest")
    public Result<List<Map<String, Object>>> latest(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "3") int limit) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(announcementService.getLatestWithReadStatus(userId, limit));
    }

    /**
     * 标记公告为已读
     */
    @PostMapping("/read")
    public Result<?> markRead(@RequestBody MarkReadRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (request.getId() != null) {
            announcementService.markRead(request.getId(), userId);
        } else if (request.getIds() != null && !request.getIds().isEmpty()) {
            announcementService.markAllRead(request.getIds(), userId);
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
