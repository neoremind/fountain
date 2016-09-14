package net.neoremind.fountain.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.rowbaselog.event.GtidEvent;

/**
 * GtidEvent事件回调器
 *
 * @author zhangxu
 */
public class GtIdEventCallback implements EventCallback<GtidEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GtIdEventCallback.class);

    @Override
    public void handle(GtidEvent event, TrxContext trxContext) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Got GtIdEvent with gtid=%s", event.getGtId()));
        }
        trxContext.setSid(event.getSid());
        trxContext.setCurrGtId(event.getGtId());
    }

}
