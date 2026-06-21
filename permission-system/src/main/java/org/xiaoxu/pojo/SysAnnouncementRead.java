package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_announcement_read")
public class SysAnnouncementRead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long announcementId;
    private Long userId;
    private LocalDateTime readTime;
}
