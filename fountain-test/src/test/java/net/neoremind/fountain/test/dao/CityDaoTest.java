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

import net.neoremind.fountain.test.po.City;
import net.neoremind.fountain.test.service.CityService;
import net.neoremind.fountain.test.support.CityHelper;

/**
 * @author zhangxu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
@ContextConfiguration("classpath:applicationContext-dbconfig.xml")
public class CityDaoTest {

    @Autowired
    private CityService cityService;

    @Test
    public void testCreate() {
        cityService.createCities(5);
        List<City> cities = cityService.getAll();
        assertThat(cities.size(), is(5));
        assertThat(cities.get(1).getCityId(), Matchers.is(CityHelper.CITY_ID_PREFIX + 1));
        assertThat(cities.get(1).getCityName(), is(CityHelper.CITY_NAME_PREFIX + 1));
    }

    @Test
    public void testCreateSingle() {
        cityService.createCity();
        List<City> cities = cityService.getAll();
        assertThat(cities.size(), is(1));
    }

    @Test
    @Transactional(propagation = Propagation.NESTED)
    public void testUpdate() {
        cityService.createCities(5);
        City city = CityHelper.getSingleCity();
        city.setCityId(CityHelper.CITY_ID_PREFIX + 2);
        cityService.updateCity(CityHelper.CITY_ID_PREFIX + 2);
        City city2 = cityService.getById(CityHelper.CITY_ID_PREFIX + 2);
        assertThat(city2, is(city));
    }

    @Test
    @Transactional(propagation = Propagation.NESTED)
    public void testDelete() {
        cityService.createCities(5);
        cityService.deleteCityByIds(CityHelper.getMultipleCityIds(2));
        List<City> cities = cityService.getAll();
        assertThat(cities.size(), is(3));
    }

}
