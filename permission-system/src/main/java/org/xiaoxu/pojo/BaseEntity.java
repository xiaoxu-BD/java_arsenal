package org.xiaoxu.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @className: BaseEntity
 * @author: xiaoxu
 * @date: 2025/11/17 15:36
 * @Version: 1.0
 * @description:
 */
@Getter
@Setter
@ToString
public class BaseEntity implements Serializable {

    private Long status;

    private String creator;

    private LocalDateTime createTime;

    private String updater;

    private LocalDateTime updateTime;

    private String deleted;

}
