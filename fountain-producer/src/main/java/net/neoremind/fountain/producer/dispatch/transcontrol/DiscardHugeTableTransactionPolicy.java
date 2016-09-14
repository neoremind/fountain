package net.neoremind.fountain.producer.dispatch.transcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.producer.dispatch.TableDataProvider;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 如果事务数据量巨大,为防止内存oom而采用的丢弃最大数据量的表的一种策略，正常来讲正常的
 * 业务不会有很大的事务，事务内的巨大数据的表的数据也不应该有业务意义，在此情况下可以将大数据表 直接丢弃。 不适用用databus5.5.
 *
 * @author hexiufeng
 */
@UnThreadSafe
public class DiscardHugeTableTransactionPolicy extends
        AbstractFullTransactionPolicy implements TransactionPolicy {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DiscardHugeTableTransactionPolicy.class);

    @Override
    protected int getTransLen() {
        int len = 0;
        for (String key : tableDataSize.keySet()) {
            TableSizeEntry entry = tableDataSize.get(key);
            if (!entry.removed) {
                len += entry.dataLen;
            }
        }
        return len;
    }

    @Override
    protected boolean isCanAddEvent(ChangeDataSet ds,
                                    TableDataProvider provider, final TableSizeEntry entry) {
        // 如果该表数据已经超大，处于删除状态，此时只记录该表在事务中的总长度
        if (entry.removed) {
            return false;
        }
        // 计算需要丢弃的表，然后从ChangeDataSet删除这些表的数据，同时设置相应的表的
        // TableSizeEntry状态为removed
        String[] discardTableArray = getDiscardTable(tableDataSize.values());
        if (discardTableArray != null && discardTableArray.length > 0) {
            for (String discardTable : discardTableArray) {
                ds.getTableDef().remove(discardTable);
                ds.getTableData().remove(discardTable);
                tableDataSize.get(discardTable).removed = true;
                if (provider.getGTId() != null) {
                    LOGGER.warn("Huge transaction table, data size is more than {}, gt id is {}, table is {}",
                            super.getMaxTransSize(), provider.getGTId(), discardTable);
                }
            }
        }
        // 可能还没有加入ChangeDataSet的数据已经是超大数据（在discardTableArray存在）
        if (entry.removed) {
            return false;
        }
        return true;
    }

    /**
     * 获取需要丢弃数据的表，这些表在事务内可能数据量大，但没有业务价值
     *
     * @param entries entries
     *
     * @return String[]
     */
    private String[] getDiscardTable(final Collection<TableSizeEntry> entries) {
        long allSize = 0L;
        List<TableSizeEntry> activeList =
                new ArrayList<TableSizeEntry>(entries.size());
        for (TableSizeEntry et : entries) {
            if (!et.isRemoved()) {
                allSize += et.getDataLen();
                activeList.add(et);
            }
        }
        Collections.sort(activeList, new Comparator<TableSizeEntry>() {

            @Override
            public int compare(TableSizeEntry o1, TableSizeEntry o2) {
                return o2.getDataLen().compareTo(o1.getDataLen());
            }

        });
        List<String> removedList = new ArrayList<String>(activeList.size());
        if (getMaxTransSize() < allSize) {
            for (TableSizeEntry et : activeList) {
                allSize -= et.getDataLen();
                removedList.add(et.getTableName());
                if (allSize <= getMaxTransSize()) {
                    break;
                }
            }
        }
        return removedList.toArray(new String[] {});
    }

}
