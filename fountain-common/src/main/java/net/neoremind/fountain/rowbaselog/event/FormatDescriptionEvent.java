package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;
import java.util.Arrays;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * binlog format描述事件，在v4版本的binlog文件中，第一个event就是FORMAT_DESCRIPTION_EVENT,
 * 它描述了接下来其他的event是如何layed out的。
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/format-description-event.html">format-description-event</a>
 * @since 2013-7-15
 */
public class FormatDescriptionEvent extends BaseLogEvent {

    private static final long serialVersionUID = -2282983822156668425L;

    public static final int NORMAL_HEADER_LENGTH = 19;

    /**
     * 2 bytes. The binary log format version. This is 4 in MySQL 5.0 and up.
     */
    public int binlogVersion;

    /**
     * 50 bytes. The MySQL server's version (example: 5.0.14-debug-log), padded with 0x00 bytes on the right.
     */
    public String serverVersion;

    /**
     * 4 bytes. Timestamp in seconds when this event was created (this is the moment when the binary log was created).
     * This value is redundant; the same value occurs in the timestamp header field.
     */
    public int binlogCreateTime;

    /**
     * 1 byte. The header length. This length - 19 gives the size of the extra headers field at the end of the header
     * for other events.
     */
    public byte eventHeaderLength;

    /**
     * (string.EOF) a array indexed by Binlog Event Type - 1 to extract the length of the event specific header.
     * <p/>
     * Variable-sized. An array that indicates the post-header lengths for all event types.
     * There is one byte per event type that the server knows about.
     * <p/>
     * event type header length这个字段，它保存了不同event的post-header长度，
     * 实际是一个bitmap，这对每个event的type - 1就是对应的值的索引就存储了post header length
     */
    public byte[] eventDataFixedPartLength;

    /**
     * 在调用者线程的使用的回调
     */
    private final FormatInfoCallback callback;

    /**
     * event header length - 常规的{@link #NORMAL_HEADER_LENGTH}
     * <p/>
     * 对于百度的MySQL用于存储gloabl transaction id
     */
    private int extraHeaderLen;

    public FormatDescriptionEvent(BinlogEventHeader eventHeader, FormatInfoCallback callback) {
        super(eventHeader);
        this.callback = callback;
    }

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        /**
         * fixed data part
         */
        this.binlogVersion = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 2);
        this.serverVersion = UnsignedNumberHelper.convertByteArray2String(MysqlValueHelper.getFixedBytes(buf, 50));
        this.binlogCreateTime = (int) UnsignedNumberHelper.convertLittleEndianUnsignedInt(buf, 4);
        this.eventHeaderLength = MysqlValueHelper.getFixedBytes(buf, 1)[0];

        /**
         * non-fixed data part
         */
        this.eventDataFixedPartLength = new byte[buf.remaining()];
        buf.get(eventDataFixedPartLength);

        /**
         * 其他工作
         */
        extraHeaderLen = this.eventHeaderLength - FormatDescriptionEvent.NORMAL_HEADER_LENGTH;
        if (callback != null) {
            callback.accept(new FormatInfo() {

                @Override
                public int getExtraHeadersLength() {
                    return extraHeaderLen;
                }

                @Override
                public int getPostHeaderLen(int eventType) {
                    return eventDataFixedPartLength[eventType - 1];
                }

            });
        }
        return this;
    }

    @Override
    public String toString() {
        return "FormatDescriptionEvent{" +
                "serverVersion='" + serverVersion + '\'' +
                ", binlogVersion=" + binlogVersion +
                ", binlogCreateTime=" + binlogCreateTime +
                ", eventHeaderLength=" + eventHeaderLength +
                ", eventDataFixedPartLength=" + Arrays.toString(eventDataFixedPartLength) +
                ", extraHeaderLen=" + extraHeaderLen +
                '}';
    }

}
