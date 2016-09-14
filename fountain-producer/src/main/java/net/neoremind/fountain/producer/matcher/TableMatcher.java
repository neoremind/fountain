package net.neoremind.fountain.producer.matcher;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.rowbaselog.event.TableMapEvent;

/**
 * 支持row base binlog协议的EventMatcher实现
 */
public class TableMatcher extends AbstractTableMatcher implements EventMatcher {

    @Override
    protected String getTableName(BaseLogEvent event) {
        TableMapEvent rle = (TableMapEvent) event;
        return rle.getFullTableName();
    }

}
