package net.neoremind.fountain.producer.datasource.ha;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.AbstractHADatasource;
import net.neoremind.fountain.datasource.TaskExcutor;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint.MysqlSyncPoint;
import net.neoremind.fountain.eventposition.DisposeEventPosition;
import net.neoremind.fountain.eventposition.EventPositionExtender;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.exception.DataSourceInvalidException;
import net.neoremind.fountain.producer.datasource.AbstractMysqlBinlogDataSource;
import net.neoremind.fountain.producer.datasource.BinlogDataSource;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategy;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategyAware;
import net.neoremind.fountain.producer.datasource.eventpositionext.SimpleEventPositionExtender;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.util.CollectionUtils;

/**
 * 基于MySQL binlog数据源的高可用实现，利用组装模式来实现
 * 
 * @author hanxu，hexiufeng, zhangxu
 */
public class HAMysqlBinlogDataSource extends
        AbstractHADatasource<BinlogDataSource> implements BinlogDataSource, BinlogDumpStrategyAware {

    private static final Logger logger = LoggerFactory.getLogger(HAMysqlBinlogDataSource.class);

    /**
     * 在当前数据源上做query、update等SQL操作的重试次数
     */
    private int ioRetryCnt = 2;

    /**
     * 记录同步点的存储器
     */
    protected DisposeEventPosition disposeEventPosition;

    /**
     * 是否需要ha自动的监控每个master的binlog position, das有时不需要自动监控
     */
    private boolean autoMonitor = false;

    /**
     * {@link #autoMonitor}为true时生效，监控周期，单位分钟
     */
    private long monitorPeriod = 1;

    /**
     * {@link #autoMonitor}为true时生效，监控启动delay，单位分钟
     */
    private long monitorInitialDelay = 1;

    private final MonitorHelper monitorHelper = new MonitorHelper();

    private final ScheduledExecutorService monitorScheduler = Executors
            .newScheduledThreadPool(1, new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "monitorScheduler-thread");
                }

            });

    public DisposeEventPosition getDisposeEventPosition() {
        return disposeEventPosition;
    }

    public void setDisposeEventPosition(
            DisposeEventPosition disposeEventPosition) {
        this.disposeEventPosition = disposeEventPosition;
    }

    public void setAutoMonitor(boolean autoMonitor) {
        this.autoMonitor = autoMonitor;
    }

    public void setMonitorPeriod(long monitorPeriod) {
        this.monitorPeriod = monitorPeriod;
    }
    
    public void setMonitorInitialDelay(long monitorInitialDelay) {
        this.monitorInitialDelay = monitorInitialDelay;
    }

    public int getIoRetryCnt() {
        return ioRetryCnt;
    }

    public void setIoRetryCnt(int ioRetryCnt) {
        if (ioRetryCnt <= 0) {
            ioRetryCnt = 2;
        }
        this.ioRetryCnt = ioRetryCnt;
    }

    public EventPositionExtender getExtender() {
        return extender;
    }

    public void setExtender(EventPositionExtender extender) {
        this.extender = extender;
    }

    /**
     * 根据EventPosition获取更多信息的扩展器
     */
    private EventPositionExtender extender = new SimpleEventPositionExtender();

    /**
     * 初始化方法，检测是否含有备用datasource，配置disposeEventPosition，extender到每一个数据源
     */
    public void init() {
        logger.info("Init HA datasource");
        checkDataSourceListEmpty();
        if (!CollectionUtils.isEmpty(mysqlDataSourceList)) {
            for (BinlogDataSource ds : mysqlDataSourceList) {
                if (ds instanceof AbstractMysqlBinlogDataSource) {
                    AbstractMysqlBinlogDataSource myDs =
                            (AbstractMysqlBinlogDataSource) ds;
                    myDs.setDisposeEventPosition(disposeEventPosition);
                    myDs.setExtender(extender);
                }
            }
        }

        if (autoMonitor) {
            logger.info(
                    "Start monitoring thread for MySQL master status, monitorPeriod:{}min, monitorInitialDelay:{}min",
                    monitorPeriod, monitorInitialDelay);
            monitorScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    moniterHandler();
                }
            }, monitorInitialDelay, monitorPeriod, TimeUnit.MINUTES);
        }
    }

    /**
     * destroy
     */
    public void destroy() {
        if (autoMonitor && !monitorScheduler.isShutdown()
                && !monitorScheduler.isTerminated()) {
            monitorScheduler.shutdown();
            try {
                monitorScheduler.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
    
    @Override
    public void close() {
        super.close();
    }

    @Override
    public SyncPoint persitSyncPoint(SyncPoint point) {
        return persitSyncPoint(point, true);
    }

    @Override
    public SyncPoint persitSyncPoint(SyncPoint point, boolean isPersist) {
        SyncPoint mergedPoint = mergePoint(point);
        
        if (isPersist) {
            disposeEventPosition.saveSyncPoint(mergedPoint);
            ThreadHolder.SYNC_POINT_CACHE.set(mergedPoint);
        }
        return mergedPoint;
    }

    @Override
    public void bindUniqName(String name) {
        disposeEventPosition.registerInstance(name);
    }

    @Override
    public void open() throws IOException, NoSuchAlgorithmException,
            TimeoutException {
        // do nothing
    }

    @Override
    public void openReplication() throws IOException, NoSuchAlgorithmException,
            TimeoutException {
        chooseMysqlDataSource();
    }

    @Override
    public byte[] readEventData() throws IOException, NoSuchAlgorithmException {
        return currentDataSource.readEventData();
    }

    @Override
    protected <T> T doHaTask(String command, String method,
            TaskExcutor<T> taskExecutor) throws NoSuchAlgorithmException,
            IOException {
        checkDataSourceListEmpty();

        for (int i = 0; i < ioRetryCnt; i++) {
            try {
                return taskExecutor.execute(command, currentDataSource);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                if (e instanceof IOException) {
                    continue;
                } else {
                    break;
                }
            }
        }

        // 选择新数据源
        logger.info("--- to choose mysql dataSource for " + method);
        chooseMysqlDataSource();

        try {
            return taskExecutor.execute(command, currentDataSource);
        } catch (Exception e) {
            throw new DataSourceInvalidException(
                    "@@@ all dataSources are not valid");
        }
    }

    @Override
    public boolean isOpenReplication() {
        return currentDataSource != null
                && currentDataSource.isOpenReplication();
    }

    @Override
    protected void prepareChoosedDatasouce(BinlogDataSource choosedDattasource)
            throws IOException, NoSuchAlgorithmException, TimeoutException {
        choosedDattasource.openReplication();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    private SyncPoint mergePoint(SyncPoint point) {
        if (!autoMonitor || !(point instanceof BinlogAndOffsetSyncPoint)) {
            return point;
        }
        BinlogAndOffsetSyncPoint binlogPoint = (BinlogAndOffsetSyncPoint) point;
        List<MysqlSyncPoint> pointList = monitorHelper.getPointList();

        MysqlSyncPoint masterPoint = binlogPoint.getSyncPointGroup().get(0);

        Iterator<MysqlSyncPoint> ie = pointList.iterator();
        while (ie.hasNext()) {
            MysqlSyncPoint cur = ie.next();
            if (cur.getMysqlId().equals(masterPoint.getMysqlId())) {
                ie.remove();
            }
        }

        pointList.add(masterPoint);

        return new BinlogAndOffsetSyncPoint(pointList);
    }

    /**
     * 监控各个mysql的当前position
     */
    private void moniterHandler() {
        logger.info("Start to monitor MySQL status.");
        List<MysqlSyncPoint> pointList = new ArrayList<MysqlSyncPoint>(8);
        for (BinlogDataSource ds : mysqlDataSourceList) {
            try {
                BinlogAndOffsetSyncPoint point =
                        ds.getMasterCurrentEventPosition();
                pointList.add(point.getSyncPointGroup().get(0));
                logger.info("Current MySQL status: {}.", point);
            } catch (Exception e) {
                // FIXME 除了currentDataSource以外其他的都没有socket连接固然出错
                logger.warn("Cannot get status from {}:{}, maybe is standby datasource", ds.getIpAddress(),
                        ds.getPort());
            }
        }
        monitorHelper.setPointList(pointList);
        logger.debug("Finish to monitor MySQL status.");
    }

    @Override
    public BinlogAndOffsetSyncPoint getMasterCurrentEventPosition()
            throws IOException {
        throw new UnsupportedOperationException("Don't support getMasterCurrentEventPosition on HAMysqlBinlogDataSource");
    }

    @Override
    public BinlogDumpStrategy getBinlogDumpStrategy() {
        if (currentDataSource instanceof BinlogDumpStrategyAware) {
            return ((BinlogDumpStrategyAware) currentDataSource).getBinlogDumpStrategy();
        }
        throw new UnsupportedOperationException("Don't support datasource without implementing BinlogDumpStrategyAware");
    }

    @Override
    public void setBinlogDumpStrategy(BinlogDumpStrategy binlogDumpStrategy) {
        if (currentDataSource instanceof BinlogDumpStrategyAware) {
            ((BinlogDumpStrategyAware) currentDataSource).setBinlogDumpStrategy(binlogDumpStrategy);
            return;
        }
        throw new UnsupportedOperationException("Don't support datasource without implementing BinlogDumpStrategyAware");
    }
}
