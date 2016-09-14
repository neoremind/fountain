package net.neoremind.fountain.runner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.haguard.zk.ZkHaGuard;
import net.neoremind.simplezkclient.SingletonZkClientProvider;

import net.neoremind.fountain.BinlogSyncBuilder;
import net.neoremind.fountain.BinlogSyncer;
import net.neoremind.fountain.DataSource;
import net.neoremind.fountain.EventConsumer;
import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.data.RowData;
import net.neoremind.fountain.eventposition.AsyncFixedRateDisposeEventPosition;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;
import net.neoremind.fountain.eventposition.LocalFileGtIdSetDisposeEventPosition;
import net.neoremind.fountain.eventposition.factory.GtIdSyncPointFactory;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogGtIdV56DumpStrategy;
import net.neoremind.fountain.producer.dispatch.transcontrol.MiniTransactionPolicy;
import net.neoremind.fountain.zk.eventposition.ZkDisposeEventPosition;

/**
 * @author zhangxu
 */
public class SingleShardBinlogSyncerTest {

    private Logger logger = LoggerFactory.getLogger(SingleShardBinlogSyncerTest.class);

    @Test
    public void testSimplePrintOut() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769").username("beidou").password("u7i8o9p0"))
                .binlogDumpStrategy(dumpStrategy)
                        //.disposeEventPosition(new ReadonlyDisposeEventPosition())
                .build();
        syncer.start();

        latch.await(120L, TimeUnit.SECONDS);
    }

    @Test
    public void testMultiDatasource() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769,10.94.37.23:8769")
                        .username("beidou,beidou")
                        .password("u7i8o9p0,beidou")
                        .slaveId("123,124"))
                .binlogDumpStrategy(dumpStrategy)
                        //.disposeEventPosition(new ReadonlyDisposeEventPosition())
                .build();
        syncer.start();

        latch.await(120L, TimeUnit.SECONDS);
    }

    @Test
    public void testCustomParameter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769,10.94.37.23:8769")
                        .username("beidou,beidou")
                        .password("u7i8o9p0,u7i8o9p0")
                        .slaveId("123,124"))
                .binlogDumpStrategy(dumpStrategy)
                .whiteTables("fountain_test.*")
                .blackTables("abc.*")
                .soTimeout(20) //一旦超时，会自动切换另外一个数据源，fountain保证多数据源之间的HA
                .transactionPolicy(new MiniTransactionPolicy()) //max 30000 row changes
                .messageQueueSize(30000) // max 20000 events
                .build();
        syncer.start();

        latch.await(120L, TimeUnit.SECONDS);
    }

    @Test
    public void testSimpleCustomPrintOut() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769").username("beidou").password("u7i8o9p0"))
                .binlogDumpStrategy(dumpStrategy)
                .consumer(new EventConsumer() {
                    @Override
                    public void onEvent(ChangeDataSet changeDataSet) {
                        printTableData(changeDataSet);
                    }

                    @Override
                    public void onSuccess(ChangeDataSet changeDataSet, DisposeEventPositionBridge positionBridge) {
                        //do nothing
                    }

                    @Override
                    public void onFail(ChangeDataSet changeDataSet, Throwable t) {
                        logger.info("Some problem occurred..." + t.getMessage());
                    }

                })
                .build();
        syncer.start();

        latch.await(120L, TimeUnit.SECONDS);
    }

    @Test
    public void testSavePosition() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769").username("beidou").password("u7i8o9p0"))
                .binlogDumpStrategy(dumpStrategy)
                .disposeEventPosition(new LocalFileGtIdSetDisposeEventPosition("/Users/baidu/work/fountain-git/test"))
                .consumer(new EventConsumer() {
                    @Override
                    public void onEvent(ChangeDataSet changeDataSet) {
                        printTableData(changeDataSet);
                    }

                    @Override
                    public void onSuccess(ChangeDataSet changeDataSet, DisposeEventPositionBridge positionBridge) {
                        positionBridge.getDisposeEventPosition(changeDataSet.getInstanceName())
                                .saveSyncPoint(changeDataSet.getSyncPoint());
                    }

                    @Override
                    public void onFail(ChangeDataSet changeDataSet, Throwable t) {
                        logger.info("Some problem occurred..." + t.getMessage());
                    }
                })
                .build();
        syncer.start();

        latch.await(120L, TimeUnit.SECONDS);
    }

    @Test
    public void testFixRateSavePosition() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        AsyncFixedRateDisposeEventPosition eventPosition = new AsyncFixedRateDisposeEventPosition();
        eventPosition.setInitDelayMs(10000);
        eventPosition.setPeriodMs(15000);
        eventPosition.setDelegate(new LocalFileGtIdSetDisposeEventPosition("/Users/baidu/work/fountain-git/test"));
        eventPosition.init();

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769").username("beidou").password("u7i8o9p0"))
                .binlogDumpStrategy(dumpStrategy)
                .disposeEventPosition(eventPosition)
                .consumer(new EventConsumer() {
                    @Override
                    public void onEvent(ChangeDataSet changeDataSet) {
                        printTableData(changeDataSet);
                    }

                    @Override
                    public void onSuccess(ChangeDataSet changeDataSet, DisposeEventPositionBridge positionBridge) {
                        positionBridge.getDisposeEventPosition(changeDataSet.getInstanceName())
                                .saveSyncPoint(changeDataSet.getSyncPoint());
                    }

                    @Override
                    public void onFail(ChangeDataSet changeDataSet, Throwable t) {
                        logger.info("Some problem occurred..." + t.getMessage());
                    }
                })
                .build();
        syncer.start();

        latch.await(120L, TimeUnit.SECONDS);
    }

    @Test
    public void testZookeeperHASavePosition() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        BinlogGtIdV56DumpStrategy dumpStrategy = new BinlogGtIdV56DumpStrategy();
        dumpStrategy.setIsChecksumSupport(true);

        SingletonZkClientProvider zkClient = new SingletonZkClientProvider();
        zkClient.setZookeeperConnectionString("127.0.0.1:2181");
        zkClient.setConnectionTimeoutMs(30000);
        zkClient.setSessionTimeoutMs(30000);

        ZkDisposeEventPosition zkEventPosition = new ZkDisposeEventPosition();
        zkEventPosition.setZkRootPath("/fountain/eventposition/testha");
        zkEventPosition.setZkClientProvider(zkClient);
        zkEventPosition.setSyncPointFactory(new GtIdSyncPointFactory());

        AsyncFixedRateDisposeEventPosition eventPosition = new AsyncFixedRateDisposeEventPosition();
        eventPosition.setInitDelayMs(10000);
        eventPosition.setPeriodMs(15000);
        eventPosition.setDelegate(zkEventPosition);
        eventPosition.init();

        ZkHaGuard haGuard = new ZkHaGuard();
        haGuard.setZkClientProvider(zkClient);
        haGuard.setLatchPath("/fountain/leader/testha");

        BinlogSyncer syncer = BinlogSyncBuilder.newBuilder()
                .producerName("producer00")
                .dataSource(DataSource.of("10.94.37.23:8769").username("beidou").password("u7i8o9p0"))
                .binlogDumpStrategy(dumpStrategy)
                .disposeEventPosition(eventPosition)
                .haGuard(haGuard)
                .consumer(new EventConsumer() {
                    @Override
                    public void onEvent(ChangeDataSet changeDataSet) {
                        printTableData(changeDataSet);
                    }

                    @Override
                    public void onSuccess(ChangeDataSet changeDataSet, DisposeEventPositionBridge positionBridge) {
                        positionBridge.getDisposeEventPosition(changeDataSet.getInstanceName())
                                .saveSyncPoint(changeDataSet.getSyncPoint());
                    }

                    @Override
                    public void onFail(ChangeDataSet changeDataSet, Throwable t) {
                        logger.info("Some problem occurred..." + t.getMessage());
                    }
                })
                .build();
        syncer.start();

        latch.await(300L, TimeUnit.SECONDS);
    }

    void printTableData(ChangeDataSet changeDataSet) {
        Map<String, List<RowData>> tableData = changeDataSet.getTableData();
        for (String tableName : tableData.keySet()) {
            logger.info("{}->size={}", tableName, tableData.get(tableName).size());
            for (RowData rowData : tableData.get(tableName)) {
                logger.info("before:" + rowData.getBeforeColumnList());
                logger.info("after:" + rowData.getAfterColumnList());
            }
        }
    }
}
