package org.xiaoxu;

import cn.hutool.core.collection.CollectionUtil;
import org.xiaoxu.pojo.Student;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */

public class StudentDatabase {
private List<Student> students;
private Map<Integer, Student> studentMap;

public StudentDatabase() {
    students = new ArrayList<>();
    studentMap = new HashMap<>();

    // 初始化数据
    Student s1 = new Student(1, "张三", 20, 85.5);
    Student s2 = new Student(2, "李四", 19, 90.0);
    Student s3 = new Student(3, "王五", 21, 78.5);
    Student s4 = new Student(4, "赵六", 19, 92.0);

    students.addAll(Arrays.asList(s1, s2, s3, s4));
    studentMap.put(s1.getId(), s1);
    studentMap.put(s2.getId(), s2);
    studentMap.put(s3.getId(), s3);
    studentMap.put(s4.getId(), s4);
}

    public Boolean updateStudentGrade(int id ,double newGrade){
        if (newGrade > 0 && newGrade <= 100){
            Set<Map.Entry<Integer, Student>> entries = studentMap.entrySet();
            for (Map.Entry<Integer, Student> entry : entries) {
                if (entry.getKey().equals(id)){
                    Student student = entry.getValue();
                    student.setGrade(newGrade);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateStudentGrade2(int id ,double newGrade){
        if (newGrade > 0 && newGrade <= 100) {
            Student student = studentMap.get(id);
            if (Objects.nonNull(student)) {
                student.setGrade(newGrade);
                return true;
            }
        }
        return false;
    }


    public List<Student> getTopStudents(int topN){

    if (topN <= 0){
        return Collections.emptyList();
    }
     return   students.stream().sorted(Comparator.comparing(Student::getGrade,Comparator.reverseOrder()))
             .limit(Math.min(topN, students.size()))
             .toList();

    }


    public Optional<Student> getYoungestStudent(){

    if (CollectionUtil.isEmpty(students)){
        return Optional.empty();
    }
    //如果最小的年龄有相等的 随便返回一个?

    return students.stream().min(Comparator.comparing(Student::getAge));
    }

    public Map<Integer,Integer> getAgeDistribution(){

      return students.stream()
                .collect(Collectors.groupingBy(Student::getAge, Collectors.counting()))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue()));


    }


    public Map<Double,Integer> getGradeStatistics(){

    return students.stream()
                .collect(Collectors.groupingBy(student -> Math.round(student.getGrade() * 10) / 10.0, Collectors.counting()))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().intValue()));


    }
public double getAverageGradeByAge(int age){

   return students.stream().filter(student -> student.getAge() == age)
           .filter(student -> student.getGrade() >= 0.0 && student.getGrade() <= 100.0)
            .mapToDouble(Student::getGrade).average().orElse(0.0);
}


    public  int  getStudentCountByGradeRange(double minGrade, double maxGrade){

        //
        if (minGrade > maxGrade){
            return 0;
        }

        return (int) students.stream().
                filter(student -> student.getGrade() >= minGrade && student.getGrade() <= maxGrade).count();



    }


    public List<Student> getStudentsByNamePrefix (String prefix){
        if (prefix == null || prefix.isEmpty()){
            return Collections.emptyList();
        }
        return students.stream().filter(student ->student.getName().toLowerCase().startsWith(prefix.toLowerCase())).toList();
    }






public boolean removeStudentById(int id){

    Student student = studentMap.get(id);
    if (Objects.isNull(student)){
        return false;
    }

    studentMap.remove(id);

    students.removeIf(studentInList -> studentInList.getId() == id);

    return true;
//    for (Student studentInList : students) {
//        if (studentInList.getId() == id){
//            //异常: ConcurrentModificationException
//           students.remove(studentInList);
//        }
//        return true;
//    }

}

public List<Student> getStudents() { return students; }
public Map<Integer, Student> getStudentMap() {
    return studentMap;
}
}
