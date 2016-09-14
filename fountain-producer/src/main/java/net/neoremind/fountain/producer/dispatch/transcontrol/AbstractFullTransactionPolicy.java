package net.neoremind.fountain.producer.dispatch.transcontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.producer.dispatch.TableDataProvider;

/**
 * 处理整个事务的抽象策略
 *
 * @author hexiufeng
 */
public abstract class AbstractFullTransactionPolicy implements
        TransactionPolicy {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractFullTransactionPolicy.class);

    /**
     * 存储事务数据
     */
    private ChangeDataSet dataSet = null;
    private boolean isRollback = false;
    /**
     * 假定的事务数据的最大数据条数是30000L，超过时放弃这个事务并记录日志
     */
    private long maxTransSize = 30000L; // 100m

    /**
     * 记录每一个表的状态
     */
    protected Map<String, TableSizeEntry> tableDataSize =
            new HashMap<String, TableSizeEntry>();

    public long getMaxTransSize() {
        return maxTransSize;
    }

    public void setMaxTransSize(long maxTransSize) {
        this.maxTransSize = maxTransSize;
    }

    @Override
    public ChangeDataSet getValidOutputDataSet() {
        // dataSet可能为空，因为一个事务的数据可能被过滤，那么在接收到XidEvent时的dataSet = null
        if (dataSet == null || isRollback || getTransLen() > maxTransSize) {
            return null;
        }
        if (getTransLen() > maxTransSize) {
            LOGGER.warn("trans {} size exceed max len", dataSet.getGtId());
            return null;
        }
        dataSet.setDataSize(this.getTransLen());
        return dataSet;
    }

    @Override
    public void acceptEvent(String instanceName, BaseLogEvent event,
                            EventCallback callback) {
        if (isRollback) {
            return;
        } else if (callback.isRollback(event)) {
            isRollback = true;
            return;
        }

        if (callback.isRowDataEvent(event)) {
            if (dataSet == null) {
                dataSet = new ChangeDataSet();
                dataSet.setInstanceName(instanceName);
            }
            addEvent2DataSet(dataSet, callback.getTableDataProvider(event));
        }

        callback.afterAccept(event);
    }

    /**
     * 获取事务中实际数据的长度
     *
     * @return length
     */
    protected abstract int getTransLen();

    /**
     * 把event加入ds
     *
     * @param ds       ChangeDataSet
     * @param provider TabbleDataProvider
     */
    private void addEvent2DataSet(ChangeDataSet ds, TableDataProvider provider) {
        String tableName = provider.getTableName();
        final TableSizeEntry entry;
        if (tableDataSize.containsKey(tableName)) {
            entry = tableDataSize.get(tableName);
        } else {
            entry = new TableSizeEntry(tableName);
        }
        // 首先记录原始大小
        tableDataSize.put(tableName, entry);
        entry.dataLen += provider.getDataLen();

        if (!isCanAddEvent(ds, provider, entry)) {
            return;
        }
        // 不需要丢弃的数据需要加入到ChangeDataSet
        if (!ds.getTableDef().containsKey(tableName)) {
            ds.getTableDef().put(tableName, provider.getColumnMeta());
        }
        List<RowData> dataRowList = null;
        if (!ds.getTableData().containsKey(tableName)) {
            dataRowList = new LinkedList<RowData>();
            ds.getTableData().put(tableName, dataRowList);
        } else {
            dataRowList = ds.getTableData().get(tableName);
        }
        dataRowList.addAll(provider.getRowData());
        ds.setGtId(provider.getGTId());
    }

    /**
     * 判断是否可以把event加入ds
     *
     * @param ds       ChangeDataSet
     * @param provider TabbleDataProvider
     * @param entry    TableSizeEntry
     *
     * @return boolean
     */
    protected abstract boolean isCanAddEvent(ChangeDataSet ds,
                                             TableDataProvider provider, final TableSizeEntry entry);

    @Override
    public boolean isInTrans() {
        return true;
    }

    @Override
    public void clear() {
        dataSet = null;
        tableDataSize.clear();
        isRollback = false;
    }

}
