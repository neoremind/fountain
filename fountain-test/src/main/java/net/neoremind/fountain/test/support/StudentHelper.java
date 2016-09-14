package net.neoremind.fountain.test.support;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.neoremind.fountain.test.po.Student;
import com.google.common.collect.Lists;

/**
 * 学生构造Model Object帮助类
 *
 * @author zhangxu
 */
public class StudentHelper {

    /**
     * unique key studentId的起始值，用于新建时候的{@link Student#studentId}的起始值
     */
    public static long STUDENT_ID_PREFIX = 10000L;

    /**
     * 记录上一次执行到的studentId，总是小于{@link #STUDENT_ID}
     */
    public static long STUDENT_ID_LAST = STUDENT_ID_PREFIX;

    /**
     * 新建学生时候，由于student id是unique key，因此需要递增
     */
    public static AtomicLong STUDENT_ID = new AtomicLong(STUDENT_ID_PREFIX);

    public static final int SCHOOL_ID_PREFIX = 100;

    public static final String STUDENT_NAME_PREFIX = "Jason-";
    public static final String DESCRIPTION_PREFIX = "描述DeSC-";
    public static final Date BIRTH_DAY_START = DateUtil.parseDate("20151210");
    public static final Date ONBOARD_TIME_START = DateUtil.parseDate("20090526");

    /**
     * 获取单个学生
     *
     * @return
     */
    public static Student getSingleStudent() {
        Student student = new Student();
        student.setStudentId(999999);
        student.setSchoolId(9999);
        student.setClassId(99);
        student.setStudentName(STUDENT_NAME_PREFIX + 9999);
        student.setAge((short) 9);
        student.setGender((byte) 9);
        student.setDescription(DESCRIPTION_PREFIX + 9999);
        student.setBirthDay(BIRTH_DAY_START);
        student.setOnboardTime(ONBOARD_TIME_START);
        student.setUpdateTime(DateUtil.getCurrentDate());
        student.setRatio(3.14f);
        return student;
    }

    /**
     * 获取多个学生的student id
     *
     * @param number
     *
     * @return
     */
    public static List<Long> getMultipleStudentIds(int number) {
        List<Long> result = Lists.newArrayListWithCapacity(number);
        for (int i = 0; i < number; i++) {
            result.add(STUDENT_ID_LAST + i);
        }
        return result;
    }

    /**
     * 获取多个学生
     * <p/>
     * student id递增
     * school id起始值+递增
     * class id递增
     * ...
     *
     * @param number
     *
     * @return
     */
    public static List<Student> getMultipleStudents(int number) {
        List<Student> result = Lists.newArrayListWithCapacity(number);
        STUDENT_ID_LAST = STUDENT_ID.get();
        for (int i = 0; i < number; i++) {
            Student student = new Student();
            student.setStudentId(STUDENT_ID.getAndIncrement());
            student.setSchoolId(SCHOOL_ID_PREFIX + i);
            student.setClassId(i);
            student.setStudentName(STUDENT_NAME_PREFIX + i);
            student.setAge((short) (i % Integer.MAX_VALUE));
            student.setGender((byte) (i % Byte.MAX_VALUE));
            student.setDescription(DESCRIPTION_PREFIX + i);
            student.setBirthDay(DateUtil.addDays(BIRTH_DAY_START, i));
            student.setOnboardTime(DateUtil.addDays(ONBOARD_TIME_START, i));
            student.setUpdateTime(DateUtil.getCurrentDate());
            student.setRatio(3.14f);
            result.add(student);
        }
        return result;
    }

}
