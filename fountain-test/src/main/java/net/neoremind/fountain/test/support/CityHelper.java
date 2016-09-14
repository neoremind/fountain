package net.neoremind.fountain.test.support;

import java.util.List;

import net.neoremind.fountain.test.po.City;
import com.google.common.collect.Lists;

/**
 * 城市Model Object构造帮助类
 *
 * @author zhangxu
 */
public class CityHelper {

    public static final int CITY_ID_PREFIX = 1000;
    public static final String CITY_NAME_PREFIX = "NYC-";

    public static City getSingleCity() {
        City city = new City();
        city.setCityId(CITY_ID_PREFIX + 9999);
        city.setCityName(CITY_NAME_PREFIX + 9999);
        return city;
    }

    public static List<Integer> getMultipleCityIds(int number) {
        List<Integer> result = Lists.newArrayListWithCapacity(number);
        for (int i = 0; i < number; i++) {
            result.add(CITY_ID_PREFIX + i);
        }
        return result;
    }

    public static List<City> getMultipleCities(int number) {
        List<City> result = Lists.newArrayListWithCapacity(number);
        for (int i = 0; i < number; i++) {
            City city = new City();
            city.setCityId(CITY_ID_PREFIX + i);
            city.setCityName(CITY_NAME_PREFIX + i);
            result.add(city);
        }
        return result;
    }

}
