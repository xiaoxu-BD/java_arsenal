package org.xiaoxu.pojo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xiaoxu.pojo.BaseEntity;

import java.time.LocalDateTime;

/**
 * @className: SystemUserDTO
 * @author: xiaoxu
 * @date: 2025/11/17 15:35
 * @Version: 1.0
 * @description:
 */
@Getter
@Setter
@ToString
public class SystemUserDTO extends BaseEntity {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Long deptId;
    private String postIds;
    private String email;
    private String mobile;
    private Long sex;
    private String avatar;

}
