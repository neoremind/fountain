package net.neoremind.fountain.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * 描述表的列信息，主要包括:
 * <ul>
 * <li>列名称</li>
 * <li>列的数据类型</li>
 * <li>是否为空标识</li>
 * <li>是否为索引以及索引类型</li>
 * <li>默认缺省值</li>
 * <li>字符集</li>
 * </ul>
 *
 * @author hanxu, hexiufeng, zhangxu
 */
public class ColumnMeta {

    /**
     * 列名
     */
    private String columnName;

    /**
     * 原始的列类型，例如int(10), varchar(256)
     */
    private String columnType;

    /**
     * 将{@link #columnType}去除括号的数据类型，例如int(10)，该值为int
     *
     * @see ColumnTypeEnum
     */
    private String generalColumnType;

    /**
     * 列类型的枚举类型
     */
    private ColumnTypeEnum typeEnum;

    /**
     * 字段编码，例如utf8_bin
     */
    private String charset;

    /**
     * 将{@link #charset}字段的短编码，例如utf8
     */
    private String shortCharset;

    /**
     * 是否为空flag
     *
     * @see MetaDefine.NOT_NULL_FLAG
     */
    private String nullFlag;

    /**
     * 索引，PRI/MUL/UNI或者为空
     *
     * @see MetaDefine.KEY_FLAG
     */
    private String keyFlag;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 冗余字段，如果是主键，则填充为auto_increment
     */
    private String extra;

    /**
     * meta值
     */
    private int metaValue;

    /**
     * 是否来自于databus
     */
    private boolean fromDatabus = false;

    /**
     * databus中的bitmap值，一个字节
     */
    private int databusColumnBitmap;

    @Override
    public String toString() {
        return "ColumnMeta{" +
                "columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", generalColumnType='" + generalColumnType + '\'' +
                ", typeEnum=" + typeEnum +
                ", charset='" + charset + '\'' +
                ", shortCharset='" + shortCharset + '\'' +
                ", nullFlag='" + nullFlag + '\'' +
                ", keyFlag='" + keyFlag + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }

    public boolean isUnsigned() {
        if (!fromDatabus) {
            return StringUtils.containsIgnoreCase(columnType, "unsigned");
        } else {
            return (databusColumnBitmap & DatabusColBitmapFlag.UNSIGNED_FLAG.getValue()) == DatabusColBitmapFlag
                    .UNSIGNED_FLAG.getValue();
        }
    }

    public boolean isNullable() {
        if (!fromDatabus) {
            return StringUtils.equalsIgnoreCase(nullFlag, MetaDefine.NOT_NULL_FLAG.YES);
        } else {
            return !((databusColumnBitmap & DatabusColBitmapFlag.NOT_NULL_FLAG.getValue()) > 0);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
        convert2GeneralColumnType(columnType);
        this.typeEnum = ColumnTypeEnum.getTypeEnumByText(generalColumnType);
    }

    private void convert2GeneralColumnType(String text) {
        int pos = text.indexOf("(");
        if (pos > 0) {
            this.generalColumnType = text.substring(0, pos);
        } else {
            generalColumnType = text;
        }

        if (StringUtils.isNotEmpty(generalColumnType) && generalColumnType.contains("unsigned")) {
            generalColumnType = generalColumnType.replace("unsigned", "").trim();
        }
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
        if (!StringUtils.isEmpty(charset)) {
            String[] array = charset.split("_");
            shortCharset = array[0];
        }
    }

    public String getShortCharset() {
        return shortCharset;
    }

    public void setShortCharset(String shortCharset) {
        this.shortCharset = shortCharset;
    }

    public String getNullFlag() {
        return nullFlag;
    }

    public void setNullFlag(String nullFlag) {
        this.nullFlag = nullFlag;
    }

    public String getKeyFlag() {
        return keyFlag;
    }

    public void setKeyFlag(String keyFlag) {
        this.keyFlag = keyFlag;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public ColumnTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(ColumnTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public int getMetaValue() {
        return metaValue;
    }

    public void setMetaValue(int metaValue) {
        this.metaValue = metaValue;
    }

    public boolean isFromDatabus() {
        return fromDatabus;
    }

    public void setFromDatabus(boolean fromDatabus) {
        this.fromDatabus = fromDatabus;
    }

    public void setGeneralColumnType(String generalColumnType) {
        this.generalColumnType = generalColumnType;
    }

    public int getDatabusColumnBitmap() {
        return databusColumnBitmap;
    }

    public void setDatabusColumnBitmap(int databusColumnBitmap) {
        this.databusColumnBitmap = databusColumnBitmap;
    }

    public String getGeneralColumnType() {
        return generalColumnType;
    }

}
