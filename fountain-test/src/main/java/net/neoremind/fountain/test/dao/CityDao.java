package net.neoremind.fountain.test.dao;

import java.util.List;

import net.neoremind.fountain.test.po.City;

/**
 * 城市DAO
 *
 * @author zhangxu
 */
public interface CityDao {

    /**
     * 批量新建
     *
     * @param dataList
     */
    void create(List<City> dataList);

    /**
     * 新建
     *
     * @param data
     */
    void createSingle(City data);

    /**
     * 单个更新
     *
     * @param data
     */
    void update(City data);

    /**
     * 按照city id删除
     *
     * @param ids
     */
    void delete(List<Integer> ids);

    /**
     * 全部删除
     */
    void deleteAll();

    /**
     * 获取所有
     *
     * @return
     */
    List<City> getAll();

    /**
     * 按照city id批量获取
     *
     * @param ids
     *
     * @return
     */
    List<City> getByIds(List<Integer> ids);

    /**
     * 按照city id获取
     *
     * @param id
     *
     * @return
     */
    City getById(Integer id);

}
