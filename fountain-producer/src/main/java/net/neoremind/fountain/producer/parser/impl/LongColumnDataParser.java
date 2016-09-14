package net.neoremind.fountain.producer.parser.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.event.data.FountainJavaTypes;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class LongColumnDataParser implements ColumnDataParser {

	@Override
	public void parse(ByteBuffer buf, ColumnData columnData, ColumnMeta meta) {
	    buf.order(ByteOrder.LITTLE_ENDIAN);
		if(!meta.isUnsigned()){
			columnData.setValue(buf.getLong());
			columnData.setJavaType(FountainJavaTypes.BIGINT);
			columnData.setSqlType(meta.getTypeEnum().getTypeValue());
		}else{
		    byte[] longBuf = new byte[8];
		    buf.get(longBuf);
    		columnData.setValue(UnsignedNumberHelper.convertLittleEndianLongByteArray2BigInteger(longBuf));
    		columnData.setJavaType(FountainJavaTypes.BIGINTEGER);
    		columnData.setSqlType(meta.getTypeEnum().getTypeValue());
		}
	}
}
