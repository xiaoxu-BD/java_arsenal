package org.xiaoxu.web_boot.service.entity;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.web_boot.entity.domain.EmployeeProject;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeProjectDTO;

import java.util.List;

public interface EmployeeProjectService extends IService<EmployeeProject> {

    List<EmployeeProject> getEmployeeProjects(Long empId);

    List<EmployeeProjectDTO> listByEmpIds(List<Long> ids);


    // 批量插入关系表
    void insertBatchByMp(Long empId, List<Long> projectIds);



    void insertBatchByMyBatis(Long empId, List<Long> projectIds);


    /**
     *
     *
     *
     * college (id, college_name)
     * major   (id, major_name, college_id)
     * course  (id, course_name, major_id)
     * teacher (id, teacher_name)
     *
     * course_teacher (course_id, teacher_id)
     *
     *
     * [
     *   {
     *     "collegeName": "...",
     *     "majors": [
     *       {
     *         "majorName": "...",
     *         "courses": [
     *           {
     *             "courseName": "...",
     *             "teachers": ["...", "..."]
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * ]
     *  for(College college : collegeList){
     *    List<Major> majors =      majorMap.getOrDefault(college.getId(),new ArrayList<>());
     *      for(Major major : majors){
     *          List<Course> courses = courseMap.getOrDefault(major.getId(),new ArrayList<>());
     *              for(Course course : courses){
     *                 List<Teacher> teachers  =        teacherMap.getOrDefault(course.getId(),new ArrayList<>());
     *                 course.setTeachers(teachers);
     *              }
     *          major.setCourses(courses);
     *      }
     *
     *     college.setMajors(majors);
     *  }
     *
     * 1. 我们先拿到: college
     * 	List<College> collegeList = collegeService.list();
     *
     * 	//嵌套关系的话我们就不走一条xml了
     * 	如果是简单的对应关系我们就走xml
     * List<Major> majorList = 	majorsService.list();
     *
     * Map<Long,List<Major>> majorMap =  majorList.stream().collect(Collectors.groupingBy(Major::getCollegeId));
     *
     *  Map<Long,List<Course>> courseMap courseService.list().stream.collect(Collectors.groupingBy(Course::getMajorId));
     *
     *  //嵌套关系
     *  select
     *      t.name as teacherName,
     *  from course_teacher ct join course c on ct.course_id = c.id
     *  join teacher t on ct.teacher_id = t.id
     *
     *
     *  Map <Long,List<Teacher> teacherMap = teacherService.list().stream().collect(Collectors.groupingBy(Teacher::getCourseId));
     */







}
