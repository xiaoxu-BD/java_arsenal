package org.xiaoxu.domain2;

import lombok.Data;

import java.util.List;

/**
 * @className: StudentDTO
 * @author: xiaoxu
 * @date: 2025/12/22 21:10
 * @Version: 1.0
 * @description: 去见PMTH2
 */
@Data
public class StudentDTO {

    private Long id;

    private String name;

    private List<Course> courses;
}
