package net.neoremind.fountain.producer.dispatch.transcontrol;

/**
 * 记录事务内每一个表的状态，主要是所占的数据的长度，指的是来自于协议的二进制数据的长度，记录该表
 * 是否已经从需要ChangeDataSet中删除。在某些策略中事务中的数据量比较多的表会被清除
 *
 * @author hexiufeng
 */
class TableSizeEntry {
    Long dataLen = 0L;
    boolean removed = false;
    final String tableName;

    public TableSizeEntry(String tableName) {
        this.tableName = tableName;
    }

    public Long getDataLen() {
        return dataLen;
    }

    public void setDataLen(Long dataLen) {
        this.dataLen = dataLen;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String getTableName() {
        return tableName;
    }
}
