package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.ColumnDataParserFactory;
import net.neoremind.fountain.event.EventHeader;
import net.neoremind.fountain.event.data.RowData;

public class RowsLogEventV1 extends RowsLogEvent {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RowsLogEventV1(EventHeader eventHeader, int headerLen, byte eventType, TableInfoCallback tableInfoCallback,
            ColumnDataParserFactory columnParserFactory) {
        super(eventHeader, headerLen, eventType, tableInfoCallback, columnParserFactory);
    }

    @Override
    protected void parseBody(ByteBuffer buf) {
        super.parseBody(buf);
        if (super.isUpdate()) {
            fillBitmap(columnUpdateUsedBitSet, buf);
        }
    }

    @Override
    protected RowData parseSingleRow(ByteBuffer buf, TableMapEvent tableMapEvent) {
        RowData rd = super.parseSingleRow(buf, tableMapEvent);
        if(super.isUpdate()){
            rd.setAfterColumnList(super.parseColumnsOfRow(buf,tableMapEvent));
        }
        return rd;
    }
}
