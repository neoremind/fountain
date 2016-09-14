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

public class DateColumnDataParser implements ColumnDataParser {

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
		buf.order(ByteOrder.LITTLE_ENDIAN);
		long date = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 3);
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set((int) (date / (16 * 32)), (int) ((date / 32) % 16 - 1),
				(int) (date % 32));
		columnData.setValue(new Timestamp(cal.getTimeInMillis()));
		columnData.setJavaType(Types.DATE);
		columnData.setSqlType(meta.getTypeEnum().getTypeValue());
	}
}
