package net.neoremind.fountain.packet;

import net.neoremind.fountain.util.ProtocolHelper;

/**
 * mysql登录验证握手包<br/>
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/connection-phase-packets
 * .html#packet-Protocol::HandshakeV10">Protocol::HandshakeV10</a>
 * @since 2013-7-9
 */
public class HandshakePacket extends MysqlPacket {

    private static final long serialVersionUID = 6362408117953514233L;

    /**
     * 0x0a protocol_version
     */
    private byte protocolVersion;

    /**
     * human-readable server version
     */
    private String version;

    /**
     * connection id
     */
    private int threadId;

    /**
     * auth_plugin_data_part_1 (string.fix_len) -- [len=8] first 8 bytes of the auth-plugin data
     */
    private byte[] seed;

    /**
     * lower 2 bytes of the <a href="http://dev.mysql.com/doc/internals/en/capability-flags
     * .html#packet-Protocol::CapabilityFlags">Protocol::CapabilityFlags</a> (optional)
     */
    private int serverCapabilities;

    /**
     * default server character-set, only the lower 8-bits <a href="http://dev.mysql
     * .com/doc/internals/en/character-set.html#packet-Protocol::CharacterSet">Protocol::CharacterSet</a> (optional)
     */
    private byte serverCharsetNumber;

    /**
     * <a href="http://dev.mysql.com/doc/internals/en/status-flags
     * .html#packet-Protocol::StatusFlags">Protocol::StatusFlags</a>
     */
    private int serverStatus;

    /**
     * auth-plugin-data-part-2
     */
    private byte[] restOfScrambleBuff;

    /**
     * 获取password加密种子
     *
     * @return bytes
     */
    public byte[] getScrambleBuff() {
        byte[] dest = new byte[seed.length + restOfScrambleBuff.length];
        System.arraycopy(seed, 0, dest, 0, seed.length);
        System.arraycopy(restOfScrambleBuff, 0, dest, seed.length, restOfScrambleBuff.length);
        return dest;
    }

    @Override
    public void fromBytes(byte[] data) {
        Position position = new Position();

        // 1 protocol version(1)
        protocolVersion = data[position.getPosition()];
        position.increase();

        // 2 version(n)
        version = ProtocolHelper.getNullTerminatedString(data, position);

        // 3 thread_id(4)
        threadId = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 4);

        // 4 salt(8)
        seed = ProtocolHelper.getFixedBytes(data, position, 8);

        // 5 filter(1) -- bypass always 0x00
        position.increase();

        // 6 server cap(2)
        serverCapabilities = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 7 charset(1)
        serverCharsetNumber = (byte) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 1);

        // 8 server status(2)
        serverStatus = (int) ProtocolHelper.getUnsignedIntByLittleEndian(data, position, 2);

        // 9 server cap(2) -- bypass
        position.increase(2);

        // 10 length of salt(1) -- bypass
        position.increase();

        // 11 filter(10) -- bypass
        position.increase(10);

        // 12 salt(12)
        restOfScrambleBuff = ProtocolHelper.getFixedBytes(data, position, 12);

        // 13 0x00 -- bypass always 0
        position.increase();
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public int getServerCapabilities() {
        return serverCapabilities;
    }

    public void setServerCapabilities(int serverCapabilities) {
        this.serverCapabilities = serverCapabilities;
    }

    public byte getServerCharsetNumber() {
        return serverCharsetNumber;
    }

    public void setServerCharsetNumber(byte serverCharsetNumber) {
        this.serverCharsetNumber = serverCharsetNumber;
    }

    public int getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(int serverStatus) {
        this.serverStatus = serverStatus;
    }

    public byte[] getRestOfScrambleBuff() {
        return restOfScrambleBuff;
    }

    public void setRestOfScrambleBuff(byte[] restOfScrambleBuff) {
        this.restOfScrambleBuff = restOfScrambleBuff;
    }

}
