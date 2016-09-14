package net.neoremind.fountain.packet;

import net.neoremind.fountain.util.ProtocolHelper;

/**
 * 描述Text Protocol结果集的头的包
 *
 * @author hanxu
 */
public class ResultSetHeaderPacket extends MysqlPacket {

    private static final long serialVersionUID = 541189365386345721L;

    private long columnCount;
    private byte[] extra;

    @Override
    public void fromBytes(byte[] data) {
        Position position = new Position();

        // 1 filed packet number(N)
        columnCount = (int) ProtocolHelper.getLengthCodedLength(data, position);

        // 2 extra
        if (position.getPosition() < data.length) {
            extra = ProtocolHelper.getFixedBytes(data, position, (data.length - position.getPosition()));
        }
    }

    public long getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(long columnCount) {
        this.columnCount = columnCount;
    }

    public byte[] getExtra() {
        return extra;
    }

    public void setExtra(byte[] extra) {
        this.extra = extra;
    }

}
