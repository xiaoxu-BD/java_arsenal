package org.xiaoxu;

import org.xiaoxu.pojo.Student;

import java.util.List;

/**
 * @className: Main
 * @author: xiaoxu
 * @date: 2025/6/8 17:23
 * @Version: 1.0
 * @description:
 */
public class Main {
    public static void main(String[] args) {
        StudentDatabase studentDatabase = new StudentDatabase();
//        List<Student> topStudents = studentDatabase.getTopStudents(5);
//        for (Student topStudent : topStudents) {
//            System.out.println(topStudent);
//        }

//        double averageGradeByAge = studentDatabase.getAverageGradeByAge(20);
//        System.out.println("Average grade by age 20: " + averageGradeByAge);


//        boolean b = studentDatabase.removeStudentById(1);
//        System.out.println(b);
//        System.out.println(studentDatabase.getStudents().size());
//        System.out.println(studentDatabase.getStudentMap().size());

//        int studentCountByGradeRange = studentDatabase.getStudentCountByGradeRange(77.0, 90.2);
//        System.out.println(studentCountByGradeRange);





//        System.out.println(studentDatabase.getYoungestStudent());


        studentDatabase.getAgeDistribution()
                .forEach((age, count) -> System.out.println("Age: " + age + ", Count: " + count));

    }



}
