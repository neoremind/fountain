package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.event.data.FountainJavaTypes;
import net.neoremind.fountain.meta.ColumnMeta;

public class DoubleColumnDataParser implements ColumnDataParser{

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
	    buf.order(ByteOrder.LITTLE_ENDIAN);
		double value = buf.getDouble();
		columnData.setValue(value);
		columnData.setJavaType(FountainJavaTypes.DOUBLE);
		columnData.setSqlType(meta.getTypeEnum().getTypeValue());
	}

}
