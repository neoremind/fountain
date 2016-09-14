package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.ColumnDataParserFactory;
import net.neoremind.fountain.event.EventHeader;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

public class RowsLogEventV2 extends RowsLogEventV1 {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public RowsLogEventV2(EventHeader eventHeader, int headerLen, byte eventType, TableInfoCallback tableInfoCallback,
            ColumnDataParserFactory columnParserFactory) {
        super(eventHeader, headerLen, eventType, tableInfoCallback, columnParserFactory);
    }
    
    private int extraDataLen;

    private byte[] extraData;


    public int getExtraDataLen() {
        return extraDataLen;
    }

    public byte[] getExtraData() {
        return extraData;
    }

    @Override
    protected void parseHeader(ByteBuffer buf) {
        super.parseHeader(buf);
        extraDataLen = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
        if (extraDataLen > 2) {
            extraData = MysqlValueHelper.getFixedBytes(buf, extraDataLen - 2);
        }
    }
}
