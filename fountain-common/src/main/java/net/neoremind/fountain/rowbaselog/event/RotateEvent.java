package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * binlog文件的结尾，通常（只要master不当机）就是ROTATE_EVENT或者STOP_EVENT
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/rotate-event.html">rotate-event</a>
 * @since 2013-7-15
 */
public class RotateEvent extends BaseLogEvent {

    private static final long serialVersionUID = -3183442443627836016L;

    /**
     * Fixed data part:
     * <p/>
     * 8 bytes. The position of the first event in the next log file.
     * Always contains the number 4 (meaning the next event starts at position 4 in the next binary log).
     * This field is not present in v1; presumably the value is assumed to be 4.
     * <p/>
     * Variable data part:
     * <p/>
     * The name of the next binary log. The filename is not null-terminated. Its length is the event size minus the
     * size of the fixed parts.
     */
    public RotateEvent(BinlogEventHeader eventHeader) {
        super(eventHeader);
    }

    /**
     * 下一个binlog事件的position
     */
    public long nextLogEventPos;

    /**
     * 下一个binglog文件名
     */
    public String nextLogFileName;

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        /**
         * fixed data part
         */
        this.nextLogEventPos = UnsignedNumberHelper.convertLittleEndianLong(buf, 8);

        byte[] nextLogFileNameBytes = MysqlValueHelper.getFixedBytes(buf, buf.remaining());
        byte[] actualLogFileName = nextLogFileNameBytes;

        /**
         * 对于支持校验和的binlog会多出来4个字节，去掉才是binlog filename
         */
        if (ThreadHolder.getTrxContext() != null) {
            if (ThreadHolder.getTrxContext().isChecksumSupport()) {
                actualLogFileName = new byte[nextLogFileNameBytes.length - 4];
                System.arraycopy(nextLogFileNameBytes, 0, actualLogFileName, 0, actualLogFileName.length);
            }
        }

        /**
         * variable data part
         */
        this.nextLogFileName =
                UnsignedNumberHelper.convertByteArray2String(actualLogFileName);
        return this;
    }

}
