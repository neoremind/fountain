package net.neoremind.fountain.event.util;

import java.util.HashMap;
import java.util.Map;

import net.neoremind.fountain.rowbaselog.event.TableMapEvent;

/**
 * @author zhangxu
 */
public class TableEventCache {

    private Map<Long, TableMapEvent> tableMap = new HashMap<Long, TableMapEvent>();
    private Map<String, Long> nameIdMap = new HashMap<String, Long>();

    public TableMapEvent getTableMapEvent(long tableId) {
        return tableMap.get(tableId);
    }

    public void setTableMapEvent(TableMapEvent tableMapEvent) {
        cleanOldTableInfo(tableMapEvent);
        nameIdMap.put(tableMapEvent.getFullTableName(), tableMapEvent.tableId);
        tableMap.put(tableMapEvent.tableId, tableMapEvent);
    }

    /**
     * 清除缓存中已经发生变化的表的TableMapEvent
     * <p>
     * tableId是mysql binlog中针对表的一个临时代号，对同一个表名来讲在不同的时期有多个tableId.
     * 当mysql内部内存不足或者表结构被修改，tableId都会发生变化，因此需要清除同一个表的不同时期的
     * TableMapEvent，以免内存溢出
     * </p>
     *
     * @param tableMapEvent TableMapEvent
     */
    private void cleanOldTableInfo(TableMapEvent tableMapEvent) {
        if (nameIdMap.containsKey(tableMapEvent.getFullTableName())) {
            Long oldId = nameIdMap.get(tableMapEvent.getFullTableName());
            if (!oldId.equals(tableMapEvent.tableId)) {
                tableMap.remove(oldId);
            }
        }
    }
}
