package net.neoremind.fountain.test.dao;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.neoremind.fountain.test.po.Student;
import net.neoremind.fountain.test.service.StudentService;
import net.neoremind.fountain.test.support.StudentHelper;

/**
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@Transactional
@ContextConfiguration("classpath:applicationContext-dbconfig.xml")
public class StudentDaoTest {

    @Autowired
    private StudentService studentService;

    @Test
    public void testCreate() {
        studentService.createStudents(5);
        List<Student> students = studentService.getAll();
        System.out.println(students);
        assertThat(students.size(), is(5));
        assertThat(students.get(1).getStudentName(), Matchers.is(StudentHelper.STUDENT_NAME_PREFIX + 1));
        assertThat(students.get(1).getStudentId(), is(StudentHelper.STUDENT_ID_LAST + 1));
    }

    @Test
    public void testCreateSingle() {
        studentService.createStudent();
        List<Student> students = studentService.getAll();
        assertThat(students.size(), is(1));
    }

    @Test
    @Transactional(propagation = Propagation.NESTED)
    public void testUpdate() {
        studentService.createStudents(5);
        Student student = StudentHelper.getSingleStudent();
        student.setStudentId(StudentHelper.STUDENT_ID_LAST + 2);
        studentService.updateStudent(StudentHelper.STUDENT_ID_LAST + 2);
        Student student2 = studentService.getById(StudentHelper.STUDENT_ID_LAST + 2);
        assertThat(student2, is(student));
    }

    @Test
    @Transactional(propagation = Propagation.NESTED)
    public void testDelete() {
        studentService.createStudents(5);
        studentService.deleteStudentByIds(StudentHelper.getMultipleStudentIds(2));
        List<Student> students = studentService.getAll();
        assertThat(students.size(), is(3));
    }

    @Test
    @Transactional(propagation = Propagation.NESTED)
    public void testDeleteAll() {
        //studentService.createStudents(5);
        studentService.deleteAll();
        List<Student> students = studentService.getAll();
        assertThat(students.size(), is(0));
    }

}
