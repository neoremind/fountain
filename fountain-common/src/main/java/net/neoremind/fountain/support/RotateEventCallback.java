package net.neoremind.fountain.support;

import net.neoremind.fountain.rowbaselog.event.RotateEvent;

/**
 * RotateEvent事件回调器
 *
 * @author zhangxu
 */
public class RotateEventCallback implements EventCallback<RotateEvent> {

    @Override
    public void handle(RotateEvent event, TrxContext trxContext) {
        trxContext.setBinlogFileName(event.nextLogFileName);
        trxContext.setNextBinlogPosition(event.nextLogEventPos);
        trxContext.setCurrBinlogPosition(event.nextLogEventPos);
    }

}
