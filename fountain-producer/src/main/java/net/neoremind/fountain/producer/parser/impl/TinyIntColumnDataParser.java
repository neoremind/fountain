package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.sql.Types;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class TinyIntColumnDataParser implements ColumnDataParser {

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
		if(!meta.isUnsigned()){
			columnData.setValue(buf.get());
			columnData.setJavaType(Types.TINYINT);
			columnData.setSqlType(meta.getTypeEnum().getTypeValue());
		}else{
		    columnData.setValue(UnsignedNumberHelper.toUnsignedByte(buf.get()));
	        columnData.setJavaType(Types.INTEGER);
	        columnData.setSqlType(ColumnTypeEnum.MYSQL_TYPE_LONG.getTypeValue());
		}
	}
}
