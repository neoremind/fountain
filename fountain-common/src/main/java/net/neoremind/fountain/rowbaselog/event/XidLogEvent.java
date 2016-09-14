package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * Transaction ID for 2PC, written whenever a COMMIT is expected.
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/xid-event.html">xid-event</a>
 * @since 2013年8月6日
 */
public class XidLogEvent extends BaseLogEvent {
    /**
     * Fixed data part:
     * <p/>
     * Empty
     * <p/>
     * Variable data part:
     * <p/>
     * 8 bytes. The XID transaction number.
     */
    private static final long serialVersionUID = 4904701088535615594L;

    public long xid;

    public XidLogEvent(BinlogEventHeader eventHeader) {
        super(eventHeader);
    }

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        xid = UnsignedNumberHelper.convertLittleEndianLong(buf, 8);
        return this;
    }
}
