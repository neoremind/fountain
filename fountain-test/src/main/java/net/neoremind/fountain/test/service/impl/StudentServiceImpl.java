package net.neoremind.fountain.test.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.neoremind.fountain.test.dao.StudentDao;
import net.neoremind.fountain.test.po.Student;
import net.neoremind.fountain.test.service.StudentService;
import net.neoremind.fountain.test.support.StudentHelper;

/**
 * 学生服务，每个方法都是事务的
 *
 * @author zhangxu
 */
@Transactional
@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentDao studentDao;

    public void createStudents(int number) {
        studentDao.create(StudentHelper.getMultipleStudents(number));
    }

    public void createStudent() {
        studentDao.createSingle(StudentHelper.getSingleStudent());
    }

    public void updateStudent(Long studentId) {
        Student student = StudentHelper.getSingleStudent();
        student.setStudentId(studentId);
        studentDao.update(student);
    }

    public void deleteStudentByIds(List<Long> ids) {
        studentDao.delete(ids);
    }

    public void deleteAll() {
        studentDao.deleteAll();
    }

    public List<Student> getAll() {
        return studentDao.getAll();
    }

    public List<Student> getByIds(List<Long> ids) {
        return studentDao.getByIds(ids);
    }

    public Student getById(Long id) {
        return studentDao.getById(id);
    }

}
