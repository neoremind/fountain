package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.ColumnDataParser;
import net.neoremind.fountain.event.ColumnDataParserFactory;
import net.neoremind.fountain.event.EventHeader;
import net.neoremind.fountain.event.RowEvent;
import net.neoremind.fountain.event.data.ColumnData;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.meta.TableMeta;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * 
 In MySQL row-based replication, each row change event contains two images, a “before” image whose columns are matched
 * against when searching for the row to be updated, and an “after” image containing the changes. Normally, MySQL logs
 * full rows (that is, all columns) for both the before and after images. However, it is not strictly necessary to
 * include every column in both images, and we can often save disk, memory, and network usage by logging only those
 * columns which are actually required.
 * 
 * When deleting a row, only the before image is logged, since there are no changed values to propagate following the
 * deletion. When inserting a row, only the after image is logged, since there is no existing row to be matched. Only
 * when updating a row are both the before and after images required, and both written to the binary log.
 * 
 * For the before image, it is necessary only that the minimum set of columns required to uniquely identify rows is
 * logged. If the table containing the row has a primary key, then only the primary key column or columns are written to
 * the binary log. Otherwise, if the table has a unique key all of whose columns are NOT NULL, then only the columns in
 * the unique key need be logged. (If the table has neither a primary key nor a unique key without any NULL columns,
 * then all columns must be used in the before image, and logged.) In the after image, it is necessary to log only the
 * columns which have actually changed.
 */

/**
 * 
 * @author hanxu03
 * 
 *         2013-7-15
 */
public class RowsLogEvent extends BaseLogEvent implements RowEvent {

    private static final long serialVersionUID = 1434103941591941107L;

    private int dataLen;
    public long tableId;
    public int reserved;

    public int columnsLength;
    public BitSet columnUsedBitSet = new BitSet();
    public BitSet columnUpdateUsedBitSet = new BitSet();
    public List<RowData> rowDataList = new ArrayList<RowData>();

    private final int headerLen;
    private final byte eventType;
    private final TableInfoCallback tableInfoCallback;
    private final ColumnDataParserFactory columnParserFactory;
    private TableMeta tableMeta;

    public TableMeta getTableMeta() {
        return tableMeta;
    }

    public RowsLogEvent(EventHeader eventHeader, int headerLen, byte eventType, TableInfoCallback tableInfoCallback,
            ColumnDataParserFactory columnParserFactory) {
        super(eventHeader);
        this.headerLen = headerLen;
        this.eventType = eventType;
        this.tableInfoCallback = tableInfoCallback;
        this.columnParserFactory = columnParserFactory;
    }

    public int getDataLen() {
        return dataLen;
    }

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        parseHeader(buf);
        TableMapEvent tableMapEvent = tableInfoCallback.getTableMapEvent(tableId);
        if (tableMapEvent == null) {
            return new UnkownBinlogEvent(super.getEventHeader());
        }
        dataLen = buf.remaining();
        parseBody(buf);
        parseAllRows(buf, tableMapEvent);
        return this;
    }

    @Override
    public boolean isInsert() {
        return EventConstant.isInsert(eventType);
    }

    @Override
    public boolean isUpdate() {
        return EventConstant.isUpdate(eventType);
    }

    @Override
    public boolean isDelete() {
        return EventConstant.isDelete(eventType);
    }

    protected void parseHeader(ByteBuffer buf) {
        if (headerLen == 6) {
            tableId = UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 4);
        } else {
            tableId = UnsignedNumberHelper.convertLittleEndianLong(buf, 6);
        }
        reserved = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
    }

    protected void parseBody(ByteBuffer buf) {
        columnsLength = (int) MysqlValueHelper.convertLengthCodedLength(buf);
        fillBitmap(columnUsedBitSet, buf);
    }

    protected void fillBitmap(BitSet bs, ByteBuffer buf) {
        int count = (columnsLength + 7) / 8;
        int index = 0;
        for (int i = 0; i < count; i++) {
            byte val = buf.get();
            for (int j = 0; j < 8 && index < columnsLength; j++, index++) {
                if ((val & EventConstant.bitArray[j]) == EventConstant.bitArray[j]) {
                    bs.set(index);
                }
            }
        }
    }

    private void parseAllRows(ByteBuffer buf, TableMapEvent tableMapEvent) {
        tableMeta =
                tableInfoCallback.getTableMeta(tableMapEvent.getFullTableName(), tableMapEvent.tableId,
                        tableMapEvent.columnInfoArray);
        while (buf.remaining() > 4) {
            rowDataList.add(parseSingleRow(buf, tableMapEvent));
        }
        if (buf.remaining() != 0 && buf.remaining() != 4) {
            throw new RuntimeException("When reaching here, MySQL binlog checksum CRC32 is enabled and 4 bytes "
                    + "after rows log event which is  "
                    + "the checksum are expected, but the bytes left are not 4");
        }
    }

    protected RowData parseSingleRow(ByteBuffer buf, TableMapEvent tableMapEvent) {
        RowData rd = new RowData();
        List<ColumnData> columnList = parseColumnsOfRow(buf, tableMapEvent);
        if (this.isInsert()) {
            rd.setAfterColumnList(columnList);
        } else {
            rd.setBeforeColumnList(columnList);
        }
        return rd;
    }

    protected final List<ColumnData> parseColumnsOfRow(ByteBuffer buf, TableMapEvent tableMapEvent) {
        BitSet rowNullBitSet = new BitSet(columnsLength);
        fillBitmap(rowNullBitSet, buf);
        int columnCount = tableMapEvent.columnsLength;
        List<ColumnData> columnList = new LinkedList<ColumnData>();
        for (int i = 0; i < columnCount; i++) {
            ColumnData colData = new ColumnData();
            if (rowNullBitSet.get(i)) {
                colData.setValue(null);
                colData.setNull(true);
            } else {
                ColumnMeta colMeta = tableMeta.getColumnMetaList().get(i);
                colMeta.setMetaValue(tableMapEvent.columnInfoArray[i].meta);
                ColumnDataParser columnParser = columnParserFactory.factory(tableMapEvent.columnInfoArray[i].type);
                columnParser.parse(buf, colData, colMeta);
                colData.setNull(false);
            }
            columnList.add(colData);
        }
        return columnList;
    }
}
