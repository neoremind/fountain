package net.neoremind.fountain.test.service;

import java.util.List;

import net.neoremind.fountain.test.po.Student;

/**
 * 学生服务接口
 *
 * @author zhangxu
 */
public interface StudentService {

    void createStudents(int number);

    void createStudent();

    void updateStudent(Long studentId);

    void deleteStudentByIds(List<Long> ids);

    void deleteAll();

    List<Student> getAll();

    List<Student> getByIds(List<Long> ids);

    Student getById(Long id);

}
