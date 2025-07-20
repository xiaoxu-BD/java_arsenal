package org.xiaoxu.web_boot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @className: Person
 * @author: xiaoxu
 * @date: 2025/7/20 15:13
 * @Version: 1.0
 * @description:
 */
@Data
public class Person {
    private Long id;

    private String name;

    private Integer age;

    private String gender;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Address> addressList;
}
