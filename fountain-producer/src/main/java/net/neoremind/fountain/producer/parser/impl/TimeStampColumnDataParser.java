package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.sql.Types;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class TimeStampColumnDataParser implements ColumnDataParser {

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
	    buf.order(ByteOrder.LITTLE_ENDIAN);
		long value = UnsignedNumberHelper.toUnsignedInt(buf.getInt());
		columnData.setValue(new Timestamp(value * 1000));
    	columnData.setJavaType(Types.TIMESTAMP);
    	columnData.setSqlType(meta.getTypeEnum().getTypeValue());
	}

}
