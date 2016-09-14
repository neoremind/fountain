package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;
import java.util.BitSet;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * @author hanxu03
 */
public class TableMapEvent extends BaseLogEvent {

    private static final long serialVersionUID = -1326376560309798269L;

    /**
     * Fixed data part:
     * <p/>
     * 6 bytes. The table ID.
     * <p/>
     * 2 bytes. Reserved for future use.
     * <p/>
     * Variable data part:
     * <p/>
     * 1 byte. The length of the database name.
     * <p/>
     * Variable-sized. The database name (null-terminated).
     * <p/>
     * 1 byte. The length of the table name.
     * <p/>
     * Variable-sized. The table name (null-terminated).
     * <p/>
     * Packed integer. The number of columns in the table.
     * <p/>
     * Variable-sized. An array of column types, one byte per column.
     * <p/>
     * Packed integer. The length of the metadata block.
     * <p/>
     * Variable-sized. The metadata block; see log_event.h for contents and format.
     * <p/>
     * Variable-sized. Bit-field indicating whether each column can be NULL, one bit per column. For this field, the
     * amount of storage required for N columns is INT((N+7)/8) bytes.
     */
    public long tableId; // 6 bytes
    public byte[] reserved; // 2 bytes

    public byte dbNameLength; // 1 byte
    public String dbName;
    public byte tableNameLength; // 1 byte
    public String tableName;
    public int columnsLength;
    // public int metaDataBlockLength;
    public ColumnInfo[] columnInfoArray;
    public BitSet columnNullMap = new BitSet();
    private final TableEventCallback callback;

    /**
     * 构造方法
     *
     * @param eventHeader 事件头
     * @param callback    回调
     */
    public TableMapEvent(BinlogEventHeader eventHeader, TableEventCallback callback) {
        super(eventHeader);
        this.callback = callback;
    }

    /**
     * 描述列信息
     *
     * @author hexiufeng
     */
    public static final class ColumnInfo {
        public ColumnTypeEnum type;
        public int meta;
    }

    public String getFullTableName() {
        return new StringBuilder(dbName).append(".").append(tableName).toString();
    }

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        /**
         * get fixed data part
         */
        this.tableId = UnsignedNumberHelper.convertLittleEndianLong(buf, 6);
        this.reserved = MysqlValueHelper.getFixedBytes(buf, 2);

        /**
         * get variable data part
         */
        this.dbNameLength = (byte) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);

        this.dbName = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getFixedBytes(buf, dbNameLength));
        // get [00]
        buf.get();
        this.tableNameLength = (byte) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1);
        this.tableName =
                UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getFixedBytes(buf, tableNameLength));
        // get [00]
        buf.get();
        // get columnsNumber and columnType
        this.columnsLength = (int) MysqlValueHelper.convertLengthCodedLength(buf);
        this.columnInfoArray = new ColumnInfo[this.columnsLength];
        for (int i = 0; i < this.columnsLength; i++) {
            ColumnInfo info = new ColumnInfo();
            this.columnInfoArray[i] = info;
            info.type =
                    ColumnTypeEnum
                            .getTypeEnumByValue((int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 1));
        }

        // get metaDataBlockLength and metaDataBlock
        int metaDataBlockLength = (int) MysqlValueHelper.convertLengthCodedLength(buf);
        byte[] metaBuf = new byte[metaDataBlockLength];
        buf.get(metaBuf);
        parseColumnMeta(metaBuf);

        fillNullBitMap(buf);
        if (null != callback) {
            callback.acceptTableMapEvent(this);
        }
        return this;
    }

    /**
     * 解析列是否为null
     *
     * @param buf byte数组数据
     */
    private void fillNullBitMap(ByteBuffer buf) {
        columnNullMap = new BitSet(columnsLength);
        int count = (columnsLength + 7) / 8;
        int index = 0;
        for (int i = 0; i < count; i++) {
            byte val = buf.get();
            for (int j = 0; j < 8 && index < columnsLength; j++, index++) {
                if ((val & EventConstant.bitArray[j]) == EventConstant.bitArray[j]) {
                    columnNullMap.set(index);
                }
            }
        }
    }

    /**
     * 解析列的meta信息
     *
     * @param buf byte数组
     */
    private void parseColumnMeta(byte[] buf) {
        ByteBuffer bbf = ByteBuffer.wrap(buf);
        for (ColumnInfo ci : columnInfoArray) {
            if (ci.type.getMetaLen() > 0) {
                ci.meta = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(bbf, ci.type.getMetaLen());
            }
            // binlog中的string类型不全部指varchar或者char，enum、set可能也是string类型
            // 如果列是string类型，那么meta的第一个字节(byte0)一定是253，即ColumnTypeEnum.MYSQL_TYPE_VAR_STRING，
            // 否则，如果(byte0 & 0x30) != 0x30,则该列的真正类型是byte|0x30(该逻辑来自于log_event.cc),
            // 否则，如果(byte0 & 0x30) == 0x30，则byte0是该列的类型(该逻辑没有文档记录，是根据数据推测出来的)
            if (ci.type == ColumnTypeEnum.MYSQL_TYPE_STRING) {
                if (ci.meta >= 256) {
                    int byte0 = ci.meta & 0xFF;
                    // 如果字段是string，则byte0必须是253，否则是其他类型
                    if ((byte0 & 0x30) == 0x30 && byte0 != ColumnTypeEnum.MYSQL_TYPE_VAR_STRING.getTypeValue()) {
                        try {
                            ci.type = ColumnTypeEnum.getTypeEnumByValue(byte0);
                        } catch (RuntimeException e) {
                            // ignore
                        }
                    }

                }
            }
        }
    }
}
