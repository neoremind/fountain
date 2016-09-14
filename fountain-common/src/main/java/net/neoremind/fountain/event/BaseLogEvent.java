package net.neoremind.fountain.event;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author zhangxu
 */
public abstract class BaseLogEvent implements Serializable {

    private static final long serialVersionUID = -2028542279856096302L;

    private EventHeader eventHeader;

    public BaseLogEvent(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
    }

    public EventHeader getEventHeader() {
        return eventHeader;
    }

    /**
     * 解析二进制数据填充event属性数据
     *
     * @param buf
     */
    public abstract BaseLogEvent parseData(ByteBuffer buf);
}
