package org.xiaoxu.entity;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @className: StudentDTO
 * @author: xiaoxu
 * @date: 2025/5/24 10:12
 * @Version: 1.0
 * @description:
 */
@Data
public class StudentDTO {
    private String id;
    private List<Long> ids;



    public List<String> convertStringList(List<Long> ids){
        return ids.stream().filter(Objects::nonNull).map(String::valueOf).toList();
    }
}
