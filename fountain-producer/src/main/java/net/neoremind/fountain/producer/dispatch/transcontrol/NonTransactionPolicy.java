package net.neoremind.fountain.producer.dispatch.transcontrol;

import java.util.LinkedList;
import java.util.List;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.producer.dispatch.TableDataProvider;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 非完整事务控制策略，需要下发的数据没有必要等待一个完整事务完成后才一次下发所有数据，而是 只发送事务的一部分数据
 *
 * @author hexiufeng
 */
@UnThreadSafe
public class NonTransactionPolicy implements TransactionPolicy {
    private ChangeDataSet dataSet;

    @Override
    public boolean isInTrans() {
        return false;
    }

    @Override
    public void clear() {
        dataSet = null;
    }

    @Override
    public ChangeDataSet getValidOutputDataSet() {
        return dataSet;
    }

    @Override
    public void acceptEvent(String instanceName, BaseLogEvent event, EventCallback callback) {
        if (callback.isRowDataEvent(event)) {
            dataSet = new ChangeDataSet();
            TableDataProvider provider = callback.getTableDataProvider(event);

            dataSet.getTableDef().put(provider.getTableName(), provider.getColumnMeta());

            List<RowData> dataRowList = null;
            dataRowList = new LinkedList<RowData>();
            dataSet.getTableData().put(provider.getTableName(), dataRowList);

            dataRowList.addAll(provider.getRowData());
            dataSet.setInstanceName(instanceName);
            dataSet.setDataSize(provider.getDataLen());
            dataSet.setGtId(provider.getGTId());
        }
        callback.afterAccept(event);
    }

}
