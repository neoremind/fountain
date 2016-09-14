package net.neoremind.fountain.packet;

import net.neoremind.fountain.exception.ParamErrorException;
import net.neoremind.fountain.util.ProtocolHelper;

/**
 * mysql eof packet
 *
 * @author hanxu
 */
public class EOFPacket extends MysqlPacket {
    private static final long serialVersionUID = -4501952768725151045L;
    public static final byte EOF_FLAG = (byte) 0xfe;

    private byte eofFlag;
    private int warningCount;
    private byte[] statusFlags;

    /**
     * 给定的数据是否是eof数据包
     *
     * @param data 给定的数据
     *
     * @return boolean
     */
    public static boolean isEOFPacket(byte[] data) {
        if (data == null || data.length == 0) {
            throw new ParamErrorException("data is not valid");
        }

        return data[0] == EOF_FLAG;
    }

    @Override
    public void fromBytes(byte[] data) {
        if (data == null || data.length == 0) {
            throw new ParamErrorException("data is not valid");
        }

        Position position = new Position();

        // 1 0xFE(1)
        eofFlag = data[position.getPosition()];
        position.increase();
        if (data.length == 1) {
            return;
        }

        // 2 warning count(2)
        warningCount = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 3 status flags(2)
        statusFlags = ProtocolHelper.getFixedBytes(data, position, 2);
    }

    public byte getEofFlag() {
        return eofFlag;
    }

    public void setEofFlag(byte eofFlag) {
        this.eofFlag = eofFlag;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public byte[] getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(byte[] statusFlags) {
        this.statusFlags = statusFlags;
    }

}
