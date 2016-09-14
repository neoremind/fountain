package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * varchar or varbinary
 */
public class VarcharStringColumnDataParser extends
        AbstractStringColumnDataParser implements ColumnDataParser {

    @Override
    protected int getPayloadLen(ByteBuffer buf, ColumnMeta meta) {
        int len = 0;
        if (meta.getMetaValue() < 256) {
            len = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);
        } else {
            len = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
        }
        return len;
    }

}
