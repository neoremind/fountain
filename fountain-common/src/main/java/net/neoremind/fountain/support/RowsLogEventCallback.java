package net.neoremind.fountain.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.rowbaselog.event.BinlogEventHeader;
import net.neoremind.fountain.rowbaselog.event.RowsLogEvent;

/**
 * RowsLogEvent事件回调器
 * <p/>
 * 主要针对百度MySQL Ares 5.1版本，在{@link BinlogEventHeader}中存在一个groupid，就是gtid，设置到{@link TrxContext#currGtId}中
 *
 * @author zhangxu
 */
public class RowsLogEventCallback<T extends RowsLogEvent> implements EventCallback<T> {

    private static final Logger logger = LoggerFactory.getLogger(RowsLogEventCallback.class);

    @Override
    public void handle(T event, TrxContext trxContext) {
        Long groupId = event.getEventHeader().getGroupId().longValue();
        if (groupId > 0L) {
            trxContext.setCurrGtId(groupId);
        }
    }

}
