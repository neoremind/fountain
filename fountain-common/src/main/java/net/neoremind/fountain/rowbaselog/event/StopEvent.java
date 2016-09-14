package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.BaseLogEvent;

/**
 * binlog文件的结尾，通常（只要master不当机）就是ROTATE_EVENT或者STOP_EVENT
 *
 * @author hanxu03, zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/stop-event.html>stop-event</a>
 * @since 2013-7-15
 */
public class StopEvent extends BaseLogEvent {

    private static final long serialVersionUID = 1025559648323251322L;

    /**
     * A master writes the event to the binary log when it shuts down
     * <p/>
     * A slave writes the event to the relay log when it shuts down or when a RESET SLAVE statement is executed
     * <p/>
     * A STOP_EVENT has no payload or post-header.
     */
    public StopEvent(BinlogEventHeader eventHeader) {
        super(eventHeader);
    }

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        return this;
    }
}
