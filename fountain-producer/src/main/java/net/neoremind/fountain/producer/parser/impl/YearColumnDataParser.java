package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Types;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class YearColumnDataParser implements ColumnDataParser {

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
		buf.order(ByteOrder.LITTLE_ENDIAN);
		columnData.setValue((int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2));
		columnData.setJavaType(Types.INTEGER);
		columnData.setSqlType(meta.getTypeEnum().getTypeValue());
	}

}
