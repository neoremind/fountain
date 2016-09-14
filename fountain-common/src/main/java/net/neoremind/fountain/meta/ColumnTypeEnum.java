package net.neoremind.fountain.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述mysql字段数据类型的枚举
 *
 * @author hexiufeng, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-query-response.html#column-type">column-type</a>
 */
public enum ColumnTypeEnum {
    MYSQL_TYPE_DECIMAL(0, "olddecimal", 2),
    MYSQL_TYPE_TINY(1, "tinyint", 0),
    MYSQL_TYPE_SHORT(2, "smallint", 0),
    MYSQL_TYPE_LONG(3, "int", 0),
    MYSQL_TYPE_FLOAT(4, "float", 1),
    MYSQL_TYPE_DOUBLE(5, "double", 1),
    MYSQL_TYPE_NULL(6, "nullbitmap", 0), // none
    MYSQL_TYPE_TIMESTAMP(7, "timestamp", 0),
    MYSQL_TYPE_LONGLONG(8, "bigint", 0),
    MYSQL_TYPE_INT24(9, "mediumint", 0),
    MYSQL_TYPE_DATE(10, "olddate", 0),
    MYSQL_TYPE_TIME(11, "time", 0),
    MYSQL_TYPE_DATETIME(12, "datetime", 0),
    MYSQL_TYPE_YEAR(13, "year", 0), // none
    MYSQL_TYPE_NEWDATE(14, "date", 0), // none
    MYSQL_TYPE_VARCHAR(15, "varchar", 2),
    MYSQL_TYPE_BIT(16, "bit", 0),
    MYSQL_TYPE_TIMESTAMP2(17, "timestamp", 0),
    MYSQL_TYPE_DATETIME2(18, "datetime", 0),
    MYSQL_TYPE_TIME2(19, "time", 0),
    MYSQL_TYPE_NEWDECIMAL(246, "decimal", 2),
    MYSQL_TYPE_ENUM(247, "enum", 2),
    MYSQL_TYPE_SET(248, "set", 2),
    MYSQL_TYPE_TINY_BLOB(249, "tinyblob", 1),
    MYSQL_TYPE_MEDIUM_BLOB(250, "mediumblob", 1),
    MYSQL_TYPE_LONG_BLOB(251, "longblob", 1),
    MYSQL_TYPE_BLOB(252, "blob", 1), MYSQL_TYPE_VAR_STRING(253, "unknown", 2), // 0xfd // 需要验证名字varbinary
    MYSQL_TYPE_STRING(254, "char", 2), // 0xfe
    MYSQL_TYPE_GEOMETRY(255, "geometry", 0); // 需要验证

    private int typeValue;
    private String typeName;
    private int metaLen;

    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>();

    static {
        TYPE_MAP.put("tinytext", "blob");
        TYPE_MAP.put("mediumtext", "blob");
        TYPE_MAP.put("text", "blob");
        TYPE_MAP.put("longtext", "blob");
        TYPE_MAP.put("binary", "char");
        TYPE_MAP.put("varbinary", "varchar");
    }

    /**
     * 私有构造反复
     *
     * @param value 以数字表示的类型
     * @param name  类型名称
     * @param mLen  该类型在binlog中对应元数据的字节数组长度
     */
    private ColumnTypeEnum(int value, String name, int mLen) {
        typeValue = value;
        typeName = name;
        metaLen = mLen;
    }

    /**
     * 根据枚举的value获取相应的枚举值
     *
     * @param val 以数字表示的类型
     *
     * @return ColumnTypeEnum
     */
    public static ColumnTypeEnum getTypeEnumByValue(int val) {

        for (ColumnTypeEnum colType : ColumnTypeEnum.values()) {
            if (colType.getTypeValue() == val) {
                return colType;
            }
        }
        throw new RuntimeException("invlid column type:" + val);
    }

    /**
     * 根据以string描述的类型获取枚举类型值
     *
     * @param val 以string描述的类型
     *
     * @return ColumnTypeEnum
     */
    public static ColumnTypeEnum getTypeEnumByText(String val) {
        String valueText = TYPE_MAP.get(val);
        if (valueText == null) {
            valueText = val;
        }
        for (ColumnTypeEnum colType : ColumnTypeEnum.values()) {
            if (colType.getTypeName().equalsIgnoreCase(valueText)) {
                return colType;
            }
        }
        throw new RuntimeException("invlid column type:" + val);
    }

    /**
     * 是否是字符串类型
     *
     * @param typeEnum ColumnTypeEnum type
     *
     * @return true or false
     */
    public boolean isStringType(ColumnTypeEnum typeEnum) {
        return typeEnum == MYSQL_TYPE_VARCHAR || typeEnum == MYSQL_TYPE_VAR_STRING || typeEnum == MYSQL_TYPE_STRING;
    }

    public int getTypeValue() {
        return typeValue;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getMetaLen() {
        return metaLen;
    }

}
