package net.neoremind.fountain.test.consumer;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.meta.ColumnMeta;

/**
 * 测试用的JVM内部变化消费者
 *
 * @author hexiufeng, zhangxu
 */
public class TestConsumer implements Consumer {

    private Logger logger = LoggerFactory.getLogger(TestConsumer.class);

    /**
     * 桥接的同步点
     */
    private DisposeEventPositionBridge bridge;

    @Override
    public <T> boolean consume(T event) {
        ChangeDataSet ds = (ChangeDataSet) event;
        logger.info("Consumer receive: " + ds);
        printTableDef(ds);
        printTableData(ds);
        savePoint(ds);
        return true;
    }

    /**
     * 打印表结构
     *
     * @param changeDataSet 数据变化
     */
    protected void printTableDef(ChangeDataSet changeDataSet) {
        Map<String, List<ColumnMeta>> tableDef = changeDataSet.getTableDef();
        logger.info("TableDef: {}", tableDef);
    }

    /**
     * 打印变化的数据
     *
     * @param changeDataSet 数据变化
     */
    protected void printTableData(ChangeDataSet changeDataSet) {
        Map<String, List<RowData>> tableData = changeDataSet.getTableData();
        for (String tableName : tableData.keySet()) {
            logger.info("TableName: " + tableName);
            for (RowData rowData : tableData.get(tableName)) {
                logger.info("Before:" + rowData.getBeforeColumnList());
                logger.info("After:" + rowData.getAfterColumnList());
            }
        }
    }

    /**
     * 持久化保存同步点
     *
     * @param ds 数据变化
     */
    private void savePoint(ChangeDataSet ds) {
        if (bridge != null) {
            bridge.getDisposeEventPosition(ds.getInstanceName()).saveSyncPoint(ds.getSyncPoint());
        }
    }

    public DisposeEventPositionBridge getBridge() {
        return bridge;
    }

    public void setBridge(DisposeEventPositionBridge bridge) {
        this.bridge = bridge;
    }

}
