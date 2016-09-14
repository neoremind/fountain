package net.neoremind.fountain.test.po;

import java.util.Date;

import com.google.common.base.Objects;

/**
 * 学生Persist Object
 * <p/>
 * mysql> desc student;
 * +--------------+----------------+------+-----+---------------------+-----------------------------+
 * | Field        | Type           | Null | Key | Default             | Extra                       |
 * +--------------+----------------+------+-----+---------------------+-----------------------------+
 * | id           | bigint(20)     | NO   | PRI | NULL                | auto_increment              |
 * | student_id   | bigint(20)     | NO   | UNI | NULL                |                             |
 * | school_id    | int(10)        | NO   | MUL | NULL                |                             |
 * | class_id     | mediumint(10)  | NO   |     | NULL                |                             |
 * | student_name | varchar(128)   | NO   |     | NULL                |                             |
 * | age          | smallint(3)    | YES  |     | 7                   |                             |
 * | gender       | tinyint(1)     | NO   |     | NULL                |                             |
 * | description  | text           | NO   |     | NULL                |                             |
 * | birth_day    | date           | NO   |     | NULL                |                             |
 * | onboard_time | datetime       | NO   |     | NULL                |                             |
 * | update_time  | timestamp      | NO   |     | 0000-00-00 00:00:00 | on update CURRENT_TIMESTAMP |
 * | ratio        | float unsigned | NO   |     | 0                   |                             |
 * +--------------+----------------+------+-----+---------------------+-----------------------------+
 *
 * @author zhangxu
 */
public class Student {

    private long id;

    private long studentId;

    private int schoolId;

    private int classId;

    private String studentName;

    private short age;

    private byte gender;

    private String description;

    private Date birthDay;

    private Date onboardTime;

    private Date updateTime;

    private float ratio;

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", schoolId=" + schoolId +
                ", classId=" + classId +
                ", studentName='" + studentName + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", description='" + description + '\'' +
                ", birthDay=" + birthDay +
                ", onboardTime=" + onboardTime +
                ", updateTime=" + updateTime +
                ", ratio=" + ratio +
                '}';
    }

    /**
     * 去掉gender和birthday验证
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Student) {
            Student that = (Student) obj;
            return Objects.equal(studentId, that.studentId)
                    && Objects.equal(schoolId, that.schoolId) && Objects.equal(classId, that.classId)
                    && Objects.equal(studentName, that.studentName) && Objects.equal(age, that.age)
                    && Objects.equal(description, that.description)
                    && Objects.equal(onboardTime, that.onboardTime);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, studentId);
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public Date getOnboardTime() {
        return onboardTime;
    }

    public void setOnboardTime(Date onboardTime) {
        this.onboardTime = onboardTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}
