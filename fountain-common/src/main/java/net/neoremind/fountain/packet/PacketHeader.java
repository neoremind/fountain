package net.neoremind.fountain.packet;

/**
 * MySQL Packet header
 * <p/>
 * 每个MySQL数据包都是由的Header和Payload组成（包括Initial Handshake Packet在内的所有数据包）。
 * Header由4个字节组成，3个字节的长度标识（<a href="http://dev.mysql.com/doc/internals/en/integer
 * .html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>），1个字节的序号。
 * Playoad长度由Header部分length指定。
 * <pre>
 * MySQL Packet
 * -------------------------
 * |      length  |   seq   |   <---- header
 * -------------------------
 * |                        |
 * |         Payload        |   <---- payload
 * |                        |
 * -------------------------
 * </pre>
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/mysql-packet.html">mysql-packet</a>
 * @since 2013-7-9
 */
public class PacketHeader {

    /**
     * 包的长度
     */
    private int packetLength;

    /**
     * 包的顺序number，官方叫做Sequence ID
     */
    private byte packetNumber;

    /**
     * 默认构造方法
     */
    public PacketHeader() {

    }

    /**
     * 构造方法
     *
     * @param packetLength payload长度
     * @param packetNumber 序号
     */
    public PacketHeader(int packetLength, byte packetNumber) {
        this.packetLength = packetLength;
        this.packetNumber = packetNumber;
    }

    /**
     * 小尾端表示
     *
     * @return 包字节
     */
    public byte[] toBytes() {
        byte[] data = new byte[4];
        data[0] = (byte) (packetLength & 0xFF);
        data[1] = (byte) (packetLength >>> 8);
        data[2] = (byte) (packetLength >>> 16);
        data[3] = packetNumber;
        return data;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(int packetLength) {
        this.packetLength = packetLength;
    }

    public byte getPacketNumber() {
        return packetNumber;
    }

    public void setPacketNumber(byte packetNumber) {
        this.packetNumber = packetNumber;
    }

}
