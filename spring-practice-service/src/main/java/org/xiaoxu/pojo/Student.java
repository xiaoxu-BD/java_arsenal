package org.xiaoxu.pojo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @className: Student
 * @author: xiaoxu
 * @date: 2025/12/4 21:31
 * @Version: 1.0
 * @description:
 */
@Data
public class Student {

    private Long id;
    @NotNull(message = "姓名不能为空")
    private String name;

    private String classId;

    @Min(value = 18,message = "年龄不能小于18岁")
    private int age;
}
