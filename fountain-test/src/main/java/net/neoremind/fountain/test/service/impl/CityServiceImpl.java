package net.neoremind.fountain.test.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.neoremind.fountain.test.dao.CityDao;
import net.neoremind.fountain.test.po.City;
import net.neoremind.fountain.test.service.CityService;
import net.neoremind.fountain.test.support.CityHelper;

/**
 * 城市服务，每个方法都是事务的
 *
 * @author zhangxu
 */
@Transactional
@Service
public class CityServiceImpl implements CityService {

    @Resource
    private CityDao cityDao;

    public void createCities(int number) {
        cityDao.create(CityHelper.getMultipleCities(number));
    }

    public void createCity() {
        cityDao.createSingle(CityHelper.getSingleCity());
    }

    public void updateCity(Integer cityId) {
        City city = CityHelper.getSingleCity();
        city.setCityId(cityId);
        cityDao.update(city);
    }

    public void deleteCityByIds(List<Integer> ids) {
        cityDao.delete(ids);
    }

    public void deleteAll() {
        cityDao.deleteAll();
    }

    public List<City> getAll() {
        return cityDao.getAll();
    }

    public List<City> getByIds(List<Integer> ids) {
        return cityDao.getByIds(ids);
    }

    public City getById(Integer id) {
        return cityDao.getById(id);
    }

}
