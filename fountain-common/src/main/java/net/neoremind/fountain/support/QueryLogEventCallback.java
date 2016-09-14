package net.neoremind.fountain.support;

import net.neoremind.fountain.rowbaselog.event.QueryLogEvent;

/**
 * QueryLogEvent事件回调器
 *
 * @author zhangxu
 */
public class QueryLogEventCallback implements EventCallback<QueryLogEvent> {

    @Override
    public void handle(QueryLogEvent event, TrxContext trxContext) {

    }

}
