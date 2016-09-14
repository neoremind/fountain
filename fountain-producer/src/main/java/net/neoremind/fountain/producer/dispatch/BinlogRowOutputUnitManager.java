package net.neoremind.fountain.producer.dispatch;

import java.math.BigInteger;
import java.util.List;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.rowbaselog.event.RowsLogEvent;
import net.neoremind.fountain.rowbaselog.event.XidLogEvent;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 数据下发单元控制的binlog协议实现
 *
 * @author hexiufeng
 */
@UnThreadSafe
public class BinlogRowOutputUnitManager extends AbstractOutputUnitManager {

    @Override
    protected boolean isValidRowDataEvent(BaseLogEvent event) {
        if (event instanceof RowsLogEvent) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean finishDataSet(BaseLogEvent event, boolean inTrans) {
        if (!inTrans) {
            return true;
        } else if (event instanceof XidLogEvent) {
            return true;
        }
        return false;
    }

    @Override
    protected TableDataProvider createTableDataProvider(BaseLogEvent event) {
        final RowsLogEvent rle = (RowsLogEvent) event;
        return new TableDataProvider() {

            @Override
            public String getTableName() {
                return rle.getTableMeta().getFullName();
            }

            @Override
            public int getDataLen() {
                return rle.rowDataList.size();
            }

            @Override
            public List<ColumnMeta> getColumnMeta() {
                return rle.getTableMeta().getColumnMetaList();
            }

            @Override
            public List<RowData> getRowData() {
                return rle.rowDataList;
            }

            @Override
            public BigInteger getGTId() {
                if (ThreadHolder.TRX_CONTEXT.get().getCurrGtId() != null) {
                    return BigInteger.valueOf(ThreadHolder.TRX_CONTEXT.get().getCurrGtId());
                }
                return null;
            }

        };
    }
}
