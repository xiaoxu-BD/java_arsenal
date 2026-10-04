package org.xiaoxu.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @className: PracticeUser
 * @description: 练习-用户表(practice_user),配合 XML mapper 教学
 */
@Data
public class PracticeUser {
    private Long id;
    private String username;
    private String phone;
    /** 等级:1普通 2白银 3黄金 */
    private Integer level;
    private LocalDateTime createTime;
}
