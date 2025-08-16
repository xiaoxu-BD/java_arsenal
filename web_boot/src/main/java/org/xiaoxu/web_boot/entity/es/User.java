package org.xiaoxu.web_boot.entity.es;

import lombok.Data;

/**
 * @className: User
 * @author: xiaoxu
 * @date: 2025/8/16 10:30
 * @Version: 1.0
 * @description: ES 文档实体类型
 */
@Data
public class User {

    private Long id;
    private String name;
    private Integer age;
}
