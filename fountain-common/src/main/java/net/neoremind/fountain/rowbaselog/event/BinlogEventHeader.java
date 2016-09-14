package net.neoremind.fountain.rowbaselog.event;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.EventHeader;
import net.neoremind.fountain.packet.Position;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.ProtocolHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * MySQL binglog header
 * <p/>
 * 对于一个binlog event来说，它分为三个部分，header，post-header以及payload。
 * 实际处理event的时候，把post-header和payload当成了一个整体body。
 * <p/>
 * MySQL的binlog event有很多版本，这里只支持version 4的，也就是从MySQL 5.1.x之后支持的版本。
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/binlog-event-header.html">binlog-event-header</a>
 * @since 2013-7-15
 */
public class BinlogEventHeader implements EventHeader {

    /**
     * seconds since unix epoch
     */
    private long timestamp;

    /**
     * Binlog类型
     *
     * @see EventConstant
     * @see BaseLogEvent
     * @see <a href="http://dev.mysql.com/doc/internals/en/binlog-event-type.html">binlog-event-type</a>
     */
    private byte typeCode;

    /**
     * server-id of the originating mysql-server.
     * Used to filter out events in circular replication.
     */
    private int serverId;

    /**
     * size of the event (header, post-header, body三个部分加起来)
     */
    private int eventLength;

    /**
     * position of the next event
     */
    private long nextPosition;

    /**
     * 暂时无用
     *
     * @see <a href="http://dev.mysql.com/doc/internals/en/binlog-event-flag.html">binlog-event-flag</a>
     */
    private int flags;

    /**
     * 针对百度的MySQL 5.1 ares定制版本，没有event header默认扩充了8个byte，用于存储gtid。
     * <p/>
     * 默认header的长度固定为19，加上这额外的8个byte，长度是27.
     */
    private byte[] extraHeaders;

    /**
     * 从{@link #extraHeaders}中提取的gtid，无符号int64类型
     */
    private BigInteger groupId = new BigInteger("-1");

    /**
     * 默认的{@link #extraHeaders}的长度，长度始终为8
     */
    private final int extraHeadersLength;

    /**
     * 构造方法
     *
     * @param extraHeadersLength
     */
    public BinlogEventHeader(int extraHeadersLength) {
        this.extraHeadersLength = extraHeadersLength;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(byte typeCode) {
        this.typeCode = typeCode;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getEventLength() {
        return eventLength;
    }

    public void setEventLength(int eventLength) {
        this.eventLength = eventLength;
    }

    public long getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(long nextPosition) {
        this.nextPosition = nextPosition;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public byte[] getExtraHeaders() {
        return extraHeaders;
    }

    public void setExtraHeaders(byte[] extraHeaders) {
        this.extraHeaders = extraHeaders;
        if (ArrayUtils.isEmpty(extraHeaders) || extraHeaders.length != 8) {
            return;
        }
        groupId = ProtocolHelper.getUnsignedLongByLittleEndian(extraHeaders, new Position(0), 8);
    }

    @Override
    public BigInteger getGroupId() {
        return groupId;
    }

    @Override
    public void parseHeader(ByteBuffer buff) {
        setTimestamp(UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff, 4));
        setTypeCode((byte) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff, 1));
        setServerId((int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff, 4));
        setEventLength((int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff, 4));
        setNextPosition((long) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff, 4));
        setFlags((int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buff, 2));

        if (extraHeadersLength > 0
                && EventConstant.ROTATE_EVENT != getTypeCode()
                && EventConstant.FORMAT_DESCRIPTION_EVENT != getTypeCode()
                && EventConstant.START_EVENT_V3 != getTypeCode()) {

            setExtraHeaders(MysqlValueHelper.getFixedBytes(buff, extraHeadersLength));
        }

    }
}
