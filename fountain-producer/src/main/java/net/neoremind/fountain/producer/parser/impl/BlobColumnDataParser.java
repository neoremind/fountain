package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class BlobColumnDataParser extends AbstractStringColumnDataParser
        implements ColumnDataParser {
    @Override
    protected int getPayloadLen(ByteBuffer buf, ColumnMeta meta) {
        return (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf,
                meta.getMetaValue());
    }
}
