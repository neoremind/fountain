package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * char or binary
 */
public class FixeStringColumnDataParser extends AbstractStringColumnDataParser implements ColumnDataParser {
    @Override
    public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
        super.parse(buf, columnData, meta);
    }

    @Override
    protected int getPayloadLen(ByteBuffer buf, ColumnMeta meta) {
        int prelen = 0;
        if (meta.getMetaValue() >= 256) {
            int byte0 = meta.getMetaValue() & 0xff;
            int byte1 = meta.getMetaValue() >> 8;
            if ((byte0 & 0x30) != 0x30) {
                /* a long CHAR() field: see #37426 */
                prelen = byte1 | (((byte0 & 0x30) ^ 0x30) << 4);
            } else {
                if (byte0 == ColumnTypeEnum.MYSQL_TYPE_STRING.getTypeValue()) {
                    prelen = byte1;
                } else {
                    throw new IllegalArgumentException(
                            String.format("!! Don't know how to handle column  meta=%d (%04X)",
                                    meta.getMetaValue(),
                                    meta.getMetaValue()));
                }
            }
        } else {
            prelen = meta.getMetaValue();
        }
        return (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, prelen < 256 ? 1 : 2);
    }
}
