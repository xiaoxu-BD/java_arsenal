package org.xiaoxu.web_boot.entity.practice;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    private String level; // 普通, VIP, SVIP
}
