package net.neoremind.fountain.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;

/**
 * 解析协议中的列数据，由协议规定的个数转化为java对象
 *
 * @author hexiufeng
 */
public interface ColumnDataParser {
    /**
     * 由byte[]数据根据协议解析出字段的元数据和字段数据
     *
     * @param buf
     * @param columnData
     * @param meta
     */
    void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta);
}
