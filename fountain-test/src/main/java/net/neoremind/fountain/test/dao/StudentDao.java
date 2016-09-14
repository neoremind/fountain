package net.neoremind.fountain.test.dao;

import java.util.List;

import net.neoremind.fountain.test.po.Student;

/**
 * 学生DAO
 *
 * @author zhangxu
 */
public interface StudentDao {

    /**
     * 批量新建
     *
     * @param dataList
     */
    void create(List<Student> dataList);

    /**
     * 新建
     *
     * @param data
     */
    void createSingle(Student data);

    /**
     * 单个更新
     *
     * @param data
     */
    void update(Student data);

    /**
     * 按照student id删除
     *
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 全部删除
     */
    void deleteAll();

    /**
     * 获取全部
     *
     * @return
     */
    List<Student> getAll();

    /**
     * 按照student id批量获取
     *
     * @param ids
     *
     * @return
     */
    List<Student> getByIds(List<Long> ids);

    /**
     * 按照student id单个获取
     *
     * @param id
     *
     * @return
     */
    Student getById(Long id);

}
