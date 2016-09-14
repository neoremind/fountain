package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class TimeColumnDataParser implements ColumnDataParser {

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
		buf.order(ByteOrder.LITTLE_ENDIAN);
		Calendar cal = Calendar.getInstance();
		long time = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 3);
		cal.set(70, 0, 1, (int)(time / 10000), (int)((time % 10000) / 100), (int)(time % 100)); 
    	columnData.setValue(new Timestamp(cal.getTimeInMillis()));
    	columnData.setJavaType(Types.TIME);
    	columnData.setSqlType(meta.getTypeEnum().getTypeValue());
	}

}
