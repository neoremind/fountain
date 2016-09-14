package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Types;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class IntegerColumnDataParser implements ColumnDataParser{
	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
	    buf.order(ByteOrder.LITTLE_ENDIAN);
		if(!meta.isUnsigned()){
			columnData.setValue(buf.getInt());
			columnData.setJavaType(Types.INTEGER);
			columnData.setSqlType(meta.getTypeEnum().getTypeValue());
		}else{
		    columnData.setValue(UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 4));
	        columnData.setJavaType(Types.BIGINT);
	        columnData.setSqlType(ColumnTypeEnum.MYSQL_TYPE_LONGLONG.getTypeValue());
		}
	}
}
