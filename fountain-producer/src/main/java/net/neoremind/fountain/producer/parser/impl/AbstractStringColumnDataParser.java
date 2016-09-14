package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.sql.Types;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * stirng 类型解析抽象类
 * <p>
 * char、varchar、string、var string包括Text都是string类型。
 * 在mysql中Text以blob方式存在
 * </p>
 */
public abstract class AbstractStringColumnDataParser implements ColumnDataParser {

    @Override
    public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
        if (isString(meta)) {
            String value = null;
            value = getString(buf, meta);
            columnData.setValue(value);
            columnData.setSqlType(meta.getTypeEnum().getTypeValue());
            columnData.setJavaType(getStringJavaType(meta));
        } else {
            columnData.setValue(parseByteArray(buf, meta));
            columnData.setJavaType(Types.BLOB);
            columnData.setSqlType(meta.getTypeEnum().getTypeValue());
        }
    }

    /**
     * 在mysql中Blob或Text类型会根据其定义的长度有不同的格式存在。
     * 其对应的有效payload的长度不同
     *
     * @param buf  数据数组
     * @param meta 字段元数据
     *
     * @return 有效负载长度
     */
    protected abstract int getPayloadLen(ByteBuffer buf, ColumnMeta meta);

    /**
     * 当前字段是否是string
     *
     * @param meta 字段元数据
     *
     * @return boolean
     */
    protected boolean isString(ColumnMeta meta) {
        String columnType = meta.getGeneralColumnType().toLowerCase();
        if (columnType.endsWith("text")
                || columnType.endsWith("char")) {
            return true;
        }
        return false;
    }

    /**
     * 字段映射到java的类型
     *
     * @param meta 字段元数据
     *
     * @return java类型
     */
    protected int getStringJavaType(ColumnMeta meta) {
        String columnType = meta.getColumnType().toLowerCase();
        if (columnType.endsWith("text")) {
            return Types.LONGVARCHAR;
        }
        if (columnType.equals("char")) {
            return Types.CHAR;
        }
        return Types.VARCHAR;
    }

    /**
     * 获取字符串值
     *
     * @param buf  bytes
     * @param meta 字段元数据
     *
     * @return string value
     */
    protected String getString(ByteBuffer buf, ColumnMeta meta) {
        if (null != meta.getShortCharset()) {
            return UnsignedNumberHelper.convertByteArray2String(parseByteArray(buf, meta),
                    CharsetSupport.convertDbCharset2Java(meta.getShortCharset()));
        } else {
            return UnsignedNumberHelper.convertByteArray2String(parseByteArray(buf, meta));
        }
    }

    /**
     * 从ByteBuffer解析有效bytes
     *
     * @param buf  ByteBuffer
     * @param meta 字段元数据
     *
     * @return 有效bytes
     */
    protected byte[] parseByteArray(ByteBuffer buf, ColumnMeta meta) {
        return MysqlValueHelper.getFixedBytes(buf, getPayloadLen(buf, meta));
    }

}
