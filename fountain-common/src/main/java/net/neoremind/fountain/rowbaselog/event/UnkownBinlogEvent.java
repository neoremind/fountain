package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.EventHeader;

public class UnkownBinlogEvent extends BaseLogEvent {

    public UnkownBinlogEvent(EventHeader eventHeader) {
        super(eventHeader);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        return null;
    }

}
