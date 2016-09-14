package net.neoremind.fountain.test.facade.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.neoremind.fountain.test.facade.MyFacade;
import net.neoremind.fountain.test.po.Student;
import net.neoremind.fountain.test.service.CityService;
import net.neoremind.fountain.test.service.StudentService;
import com.google.common.collect.Lists;

/**
 * @author zhangxu
 */
@Service
public class MyFacadeImpl implements MyFacade {

    @Resource
    private StudentService studentService;

    @Resource
    private CityService cityService;

    @Transactional
    public void doHybridStudents() {
        List<Student> allStudents = studentService.getAll();
        Student toUpdate = allStudents.get(5);

        // 更新第5个
        studentService.updateStudent(toUpdate.getStudentId());

        // 删除第6、8、9个
        List<Long> toDeleteIds = Lists.newArrayList(
                allStudents.get(6).getStudentId(),
                allStudents.get(8).getStudentId(),
                allStudents.get(9).getStudentId());
        studentService.deleteStudentByIds(toDeleteIds);

        // 新建5个
        studentService.createStudents(5);
    }

    @Transactional
    public void doMultiTable() {
        // 新建5个student
        studentService.createStudents(5);

        // 新建3个city
        cityService.createCities(3);
    }

}
