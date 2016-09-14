package net.neoremind.fountain.meta;

import java.util.List;

/**
 * 表结构
 *
 * @author hanxu, zhangxu
 */
public class TableMeta {

    /**
     * 表全名，例如database.table
     */
    private String fullName;

    /**
     * 列信息
     *
     * @see ColumnMeta
     */
    private List<ColumnMeta> columnMetaList;

    /**
     * Table ID
     * <p/>
     * <a href="http://dev.mysql.com/doc/internals/en/rows-event.html">rows-event</a>中会存在table
     * id，MySQL给每个表都设置一个标识，但是也许会变，因为alter table或者rotate binlog event等原因，master会改变某个table的table id
     */
    private long tableId;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<ColumnMeta> getColumnMetaList() {
        return columnMetaList;
    }

    public void setColumnMetaList(List<ColumnMeta> columnMetaList) {
        this.columnMetaList = columnMetaList;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

}
