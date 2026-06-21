package org.xiaoxu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.SysAnnouncementMapper;
import org.xiaoxu.mapper.SysAnnouncementReadMapper;
import org.xiaoxu.pojo.SysAnnouncement;
import org.xiaoxu.pojo.SysAnnouncementRead;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统公告服务
 */
@Service
public class AnnouncementService {

    @Resource
    private SysAnnouncementMapper announcementMapper;

    @Resource
    private SysAnnouncementReadMapper readMapper;

    /**
     * 分页查询公告（管理员）
     */
    public IPage<SysAnnouncement> pageQuery(int current, int size, String title, Integer status) {
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAnnouncement::getDeleted, "0");
        if (title != null && !title.isBlank()) {
            wrapper.like(SysAnnouncement::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(SysAnnouncement::getStatus, status);
        }
        wrapper.orderByDesc(SysAnnouncement::getCreateTime);
        return announcementMapper.selectPage(new Page<>(current, size), wrapper);
    }

    /**
     * 获取用户未读的已发布公告（登录后弹窗展示）
     */
    public List<SysAnnouncement> getUnread(Long userId) {
        // 1. 查所有已发布公告
        List<SysAnnouncement> published = announcementMapper.selectList(
                new LambdaQueryWrapper<SysAnnouncement>()
                        .eq(SysAnnouncement::getDeleted, "0")
                        .eq(SysAnnouncement::getStatus, 1)
                        .orderByDesc(SysAnnouncement::getPublishTime));
        if (published.isEmpty()) return List.of();

        // 2. 查该用户已读的公告 ID
        List<Long> readIds = readMapper.selectList(
                new LambdaQueryWrapper<SysAnnouncementRead>()
                        .eq(SysAnnouncementRead::getUserId, userId)
                        .in(SysAnnouncementRead::getAnnouncementId,
                                published.stream().map(SysAnnouncement::getId).collect(Collectors.toList())))
                .stream()
                .map(SysAnnouncementRead::getAnnouncementId)
                .collect(Collectors.toList());

        // 3. 过滤出未读的
        return published.stream()
                .filter(a -> !readIds.contains(a.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 标记公告为已读
     */
    public void markRead(Long announcementId, Long userId) {
        // 检查是否已读
        Long count = readMapper.selectCount(
                new LambdaQueryWrapper<SysAnnouncementRead>()
                        .eq(SysAnnouncementRead::getAnnouncementId, announcementId)
                        .eq(SysAnnouncementRead::getUserId, userId));
        if (count > 0) return;

        SysAnnouncementRead read = new SysAnnouncementRead();
        read.setAnnouncementId(announcementId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        readMapper.insert(read);
    }

    /**
     * 批量标记已读
     */
    public void markAllRead(List<Long> announcementIds, Long userId) {
        for (Long id : announcementIds) {
            markRead(id, userId);
        }
    }

    public void create(SysAnnouncement announcement) {
        announcement.setStatus(0);
        announcement.setDeleted("0");
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
    }

    public void update(SysAnnouncement announcement) {
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
    }

    public void publish(Long id, String publisher) {
        SysAnnouncement update = new SysAnnouncement();
        update.setId(id);
        update.setStatus(1);
        update.setPublisher(publisher);
        update.setPublishTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(update);
    }

    public void withdraw(Long id) {
        SysAnnouncement update = new SysAnnouncement();
        update.setId(id);
        update.setStatus(2);
        update.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(update);
    }

    public void delete(Long id) {
        SysAnnouncement update = new SysAnnouncement();
        update.setId(id);
        update.setDeleted("1");
        update.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(update);
    }
}
