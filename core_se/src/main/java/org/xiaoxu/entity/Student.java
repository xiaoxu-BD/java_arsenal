package org.xiaoxu.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @className: Student
 * @author: xiaoxu
 * @date: 2025/5/23 22:38
 * @Version: 1.0
 * @description:
 */
@Data

public class Student implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
//    private List<String> ids;
    private String name;
    private Integer age;
}
