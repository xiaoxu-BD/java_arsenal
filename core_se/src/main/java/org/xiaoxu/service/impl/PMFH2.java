package org.xiaoxu.service.impl;

import org.xiaoxu.domain.DeptDTO;
import org.xiaoxu.domain2.Course;
import org.xiaoxu.domain2.Student;
import org.xiaoxu.domain2.StudentCourse;
import org.xiaoxu.domain2.StudentDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className: PMFH2
 * @author: xiaoxu
 * @date: 2025/12/22 21:11
 * @Version: 1.0
 * @description:
 */
public class PMFH2 {




    public List<StudentDTO> getResult(){

        List<StudentDTO> result = new ArrayList<>();

        List<Student> students = this.getStudents();

        List<Course> courses = this.getCourses();

        Map<Long, Course> courseMap = courses.stream().collect(Collectors.toMap(Course::getId, course -> course));

        //关联关系
        List<StudentCourse> studentCourses = getStudentCourses();

        Map<Long,List<Course>> studentCourseMap = new HashMap<>();

        for (StudentCourse studentCourse : studentCourses) {
            //如果键不存在，则计算并放入值
            //computeIfAbsent 最后返回的是map中value 类型的
            studentCourseMap.computeIfAbsent(studentCourse.getStuId(), k -> new ArrayList<>())
                            .add(courseMap.get(studentCourse.getCourseId()));
        }


        for (Student student : students) {
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setId(student.getId());
            studentDTO.setName(student.getName());
            studentDTO.setCourses(studentCourseMap.get(student.getId()));
            result.add(studentDTO);
        }

        return result;

    }






    // 模拟

    public List<Student> getStudents(){
        return List.of();
    }

    public List<Course> getCourses(){
        return List.of();
    }

    public List<StudentCourse> getStudentCourses(){
        return List.of();
    }
}
