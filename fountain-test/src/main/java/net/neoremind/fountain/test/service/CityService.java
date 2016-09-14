package net.neoremind.fountain.test.service;

import java.util.List;

import net.neoremind.fountain.test.po.City;

/**
 * 城市服务接口
 *
 * @author zhangxu
 */
public interface CityService {

    void createCities(int number);

    void createCity();

    void updateCity(Integer cityId);

    void deleteCityByIds(List<Integer> ids);

    void deleteAll();

    List<City> getAll();

    List<City> getByIds(List<Integer> ids);

    City getById(Integer id);

}
