package net.neoremind.fountain.support;

import net.neoremind.fountain.rowbaselog.event.BinlogEventHeader;
import net.neoremind.fountain.rowbaselog.event.XidLogEvent;

/**
 * XidLogEvent事件回调器
 *
 * @author zhangxu
 */
public class XidLogEventCallback implements EventCallback<XidLogEvent> {

    @Override
    public void handle(XidLogEvent event, TrxContext trxContext) {
        long pos = ((BinlogEventHeader) event.getEventHeader()).getNextPosition();
        trxContext.setCurrBinlogPosition(trxContext.getNextBinlogPosition());
        trxContext.setNextBinlogPosition(pos);
    }

}
