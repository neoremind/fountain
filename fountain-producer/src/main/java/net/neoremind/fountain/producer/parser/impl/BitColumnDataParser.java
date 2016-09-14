package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.sql.Types;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class BitColumnDataParser implements ColumnDataParser {

    @Override
    public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
        
        final int nbits = ((meta.getMetaValue() >> 8) * 8) + (meta.getMetaValue() & 0xff);
        int len = (nbits + 7) / 8;
        if (nbits > 1)
        {
            byte[] binary = MysqlValueHelper.getFixedBytes(buf, len);
            columnData.setValue(binary);
        }
        else
        {
            final int bit = (int)UnsignedNumberHelper.convertLittleEndianLong(buf,  1);
            columnData.setValue((bit != 0) ? Boolean.TRUE : Boolean.FALSE);
        }
        columnData.setJavaType(Types.BIT);
    }

}
