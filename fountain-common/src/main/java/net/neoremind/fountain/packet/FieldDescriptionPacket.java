package net.neoremind.fountain.packet;

import net.neoremind.fountain.meta.ColumnTypeEnum;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * mysql sql field 描述包
 *
 * @author hanxu, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-query-response
 * .html#packet-Protocol::ColumnDefinition">Protocol::ColumnDefinition</a>
 * @since 2013年8月3日
 */
public class FieldDescriptionPacket extends MysqlPacket {
    private static final long serialVersionUID = 1585203012794038075L;

    /**
     * catalog (always "def")
     */
    private String catalog;

    /**
     * schema-name
     */
    private String dbName;

    /**
     * virtual table-name
     */
    private String tableAlias;

    /**
     * physical table-name
     */
    private String tableName;

    /**
     * virtual column name
     */
    private String columnAlias;

    /**
     * physical column name
     */
    private String columnName;

    /**
     * 字符编码
     */
    private int character;

    /**
     * maximum length of the field
     */
    private long length;

    /**
     * 值采用{@link ColumnTypeEnum}
     *
     * @see <a href="http://dev.mysql.com/doc/internals/en/com-query-response.html#column-type">column-type</a>
     */
    private byte type;

    /**
     * flags
     */
    private int flags;

    /**
     * max shown decimal digits
     * <p/>
     * 0x00 for integers and static strings
     * <p/>
     * 0x1f for dynamic strings, double, float
     * <p/>
     * 0x00 to 0x51 for decimals
     */
    private byte decimal;

    /**
     * 其他字段，目前仅在COM_FIELD_LIST使用
     */
    private byte[] defaultExtra;

    @Override
    public void fromBytes(byte[] data) {

        Position position = new Position();

        // 1 catalog(N)
        int dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
        catalog = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));

        // 2 db(N)
        dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
        dbName = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));

        // 3 tables_alias(N)
        dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
        tableAlias = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));

        // 4 org_table(N)
        dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
        tableName = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));

        // 5 column_alias(N)
        dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
        columnAlias = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));

        // 6 org_column(N)
        dataLength = (int) ProtocolHelper.getLengthCodedLength(data, position);
        columnName = new String(ProtocolHelper.getFixedBytes(data, position, dataLength));

        // 7 filter(1) always 0c
        position.increase();

        // 8 charsetnr(1)
        character = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 9 length(4)
        length = ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 4);

        // 10 type(1)
        type = (byte) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 1);

        // 11 flags(2)
        flags = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 12 decimal(1)
        decimal = (byte) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 1);

        // 13 filter(2) always [00][00]
        position.increase(2);

        // 14 default(N)
        if (position.getPosition() < data.length) {
            defaultExtra = ProtocolHelper.getFixedBytes(data, position, (data.length - position.getPosition()));
        }
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnAlias() {
        return columnAlias;
    }

    public void setColumnAlias(String columnAlias) {
        this.columnAlias = columnAlias;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public byte getDecimal() {
        return decimal;
    }

    public void setDecimal(byte decimal) {
        this.decimal = decimal;
    }

    public byte[] getDefaultExtra() {
        return defaultExtra;
    }

    public void setDefaultExtra(byte[] defaultExtra) {
        this.defaultExtra = defaultExtra;
    }

}
