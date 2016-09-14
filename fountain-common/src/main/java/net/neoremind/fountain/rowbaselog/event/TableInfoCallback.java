package net.neoremind.fountain.rowbaselog.event;

import net.neoremind.fountain.meta.TableMeta;

/**
 * 获取表信息的回调接口描述
 *
 * @author hexiufeng
 */
public interface TableInfoCallback {
    /**
     * 根据tableId获取缓存的TableMapEvent
     *
     * @param tableId mysql内部记录表的id
     *
     * @return TableMapEvent
     */
    TableMapEvent getTableMapEvent(long tableId);

    /**
     * 根据表名和tableid获取meta信息
     *
     * @param tableName  表名
     * @param tableId    table id
     * @param columnInfo 列信息
     *
     * @return meta
     */
    TableMeta getTableMeta(String tableName, long tableId, TableMapEvent.ColumnInfo[] columnInfo);
}
