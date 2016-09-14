package net.neoremind.fountain.event.data;

import net.neoremind.fountain.meta.ColumnTypeEnum;

/**
 * 列数据
 *
 * @author zhangxu
 */
public class ColumnData {

    /**
     * 数据值
     */
    private Object value;

    /**
     * 数据类型
     *
     * @see java.sql.Types
     */
    private int javaType;

    /**
     * {@link #value}是否为空，通过解析binlog中的bitmap位判断得出
     */
    private boolean isNull;

    /**
     * sql type，fountain内部使用的类型枚举
     *
     * @see ColumnTypeEnum
     */
    private int sqlType = -1;

    @Override
    public String toString() {
        return "ColumnData{" +
                "value=" + value +
                ", javaType=" + javaType +
                ", isNull=" + isNull +
                ", sqlType=" + sqlType +
                '}';
    }

    public void setIsNull(boolean isNull) {
        this.isNull = isNull;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getJavaType() {
        return javaType;
    }

    public void setJavaType(int javaType) {
        this.javaType = javaType;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean isNull) {
        this.isNull = isNull;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

}
