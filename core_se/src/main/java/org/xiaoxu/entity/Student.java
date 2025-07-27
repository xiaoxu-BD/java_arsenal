package org.xiaoxu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @className: Student
 * @author: xiaoxu
 * @date: 2025/5/23 22:38
 * @Version: 1.0
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student implements Serializable,Comparable<Student> {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
//    private List<String> ids;
    private String name;
    private Integer age;

    public  List<String> convertToList(String id){

        return Arrays.stream(id.split(",")).toList();
    }

    @Override
    public int compareTo(Student o) {
        int flag = this.name.compareTo(o.name);
        if (flag == 0){
            return this.age.compareTo(o.age);
        }
        return flag;
    }
}
