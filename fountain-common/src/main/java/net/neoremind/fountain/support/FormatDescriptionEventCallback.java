package net.neoremind.fountain.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.rowbaselog.event.FormatDescriptionEvent;

/**
 * FormatDescriptionEvent事件回调器
 *
 * @author zhangxu
 */
public class FormatDescriptionEventCallback implements EventCallback<FormatDescriptionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(FormatDescriptionEventCallback.class);

    @Override
    public void handle(FormatDescriptionEvent event, TrxContext trxContext) {
        if (logger.isDebugEnabled()) {
            logger.info("Got " + event);
        }
        trxContext.setFmtDescEvent(event);
    }

}
