package net.neoremind.fountain.test.po;

import com.google.common.base.Objects;

/**
 * 城市Persist Object
 * <p/>
 * mysql> desc city;
 * +-----------+---------------+------+-----+---------+----------------+
 * | Field     | Type          | Null | Key | Default | Extra          |
 * +-----------+---------------+------+-----+---------+----------------+
 * | id        | bigint(20)    | NO   | PRI | NULL    | auto_increment |
 * | city_id   | int(10)       | NO   | UNI | NULL    |                |
 * | city_name | varchar(1024) | NO   |     | NULL    |                |
 * +-----------+---------------+------+-----+---------+----------------+
 *
 * @author zhangxu
 */
public class City {

    private long id;

    private int cityId;

    private String cityName;

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", cityId=" + cityId +
                ", cityName='" + cityName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof City) {
            City that = (City) obj;
            return Objects.equal(cityId, that.cityId)
                    && Objects.equal(cityName, that.cityName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, cityId);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
