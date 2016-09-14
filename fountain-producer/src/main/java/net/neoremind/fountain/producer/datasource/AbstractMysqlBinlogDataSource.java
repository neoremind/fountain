package net.neoremind.fountain.producer.datasource;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import net.neoremind.fountain.datasource.AbstractMysqlDataSource;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.eventposition.BaiduGroupIdSyncPoint;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.DisposeEventPosition;
import net.neoremind.fountain.eventposition.EventPositionExtender;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.packet.PacketHeader;
import net.neoremind.fountain.packet.ResultSetPacket;
import net.neoremind.fountain.packet.RowValuePacket;
import net.neoremind.fountain.producer.AbstractProducer;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategy;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategyAware;
import net.neoremind.fountain.producer.datasource.eventpositionext.SimpleEventPositionExtender;
import net.neoremind.fountain.producer.datasource.slaveid.SlaveIdGenerateStrategy;
import net.neoremind.fountain.producer.exception.NormalSocketTimeoutException;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.util.CollectionUtils;
import net.neoremind.fountain.util.MysqlCommonConstants;
import net.neoremind.fountain.util.ProtocolHelper;
import net.neoremind.fountain.util.SocketHelper;
import net.neoremind.fountain.rowbaselog.event.BinlogEventHeader;

/**
 * 描述BinlogDataSource的数据源,建议BinlogDataSource接口的实现都继承本类
 *
 * @author hexiufeng, zhangxu
 */
public abstract class AbstractMysqlBinlogDataSource extends AbstractMysqlDataSource
        implements BinlogDataSource, BinlogDumpStrategyAware {

    /**
     * 使用COM_QUERY发送查询请求使用获取结果的列数预期
     */
    protected static final int MASTER_STATUS_FIELD_MIN_CNT = 4;

    /**
     * binlog packet header长度，默认为4
     *
     * @see BinlogEventHeader
     */
    protected static final int BINLOG_PACKET_HEADER_LENGTH = 4;

    /**
     * 复制socket
     */
    protected Socket replicationSocket = null;

    /**
     * 默认的fountain的slaveId，默认是10，绝对不能为0
     */
    protected int slaveId = 10;

    /**
     * slaveId生成器，当调用{@link #setSlaveIdGenerateStrategy(SlaveIdGenerateStrategy)}时
     * 会覆盖掉{@link #slaveId}。
     */
    protected SlaveIdGenerateStrategy slaveIdGenerateStrategy;

    /**
     * 获取同步点的帮助类，例如可以从线程、文件、配置中获取
     */
    protected DisposeEventPosition disposeEventPosition;

    /**
     * 对于获取来的同步点{@link DisposeEventPosition}进行扩展的辅助类
     */
    protected EventPositionExtender extender = new SimpleEventPositionExtender();

    /**
     * 用于通过Group的方式配置多数据源，将配置文件中的同步点设置为datasource的同步点
     *
     * @param syncPoint 同步点
     */
    public abstract void apply(SyncPoint syncPoint);

    /**
     * 启动复制
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws TimeoutException
     */
    protected abstract void startReplication() throws IOException, NoSuchAlgorithmException, TimeoutException;

    /**
     * 获取配置的数据复制点
     *
     * @return
     */
    protected abstract SyncPoint getConfigureStartPosition();

    @Override
    protected void printMysqlInfo() throws IOException {
        super.printMysqlInfo();
        if (slaveIdGenerateStrategy == null) {
            getLogger().info("slaveId is {}", slaveId);
        }
    }

    @Override
    public SyncPoint persitSyncPoint(SyncPoint point) {
        return persitSyncPoint(point, true);
    }

    @Override
    public SyncPoint persitSyncPoint(SyncPoint point, boolean isPersist) {
        if (isPersist) {
            disposeEventPosition.saveSyncPoint(point);
            ThreadHolder.SYNC_POINT_CACHE.set(point);
        }
        return point;
    }

    @Override
    public void bindUniqName(String name) {
        disposeEventPosition.registerInstance(name);
    }

    @Override
    public boolean isOpen() {
        return replicationSocket != null;
    }

    @Override
    public void openReplication() throws IOException, NoSuchAlgorithmException,
            TimeoutException {
        super.open();
        startReplication();
    }

    @Override
    protected void applySocket(Socket socket) {
        replicationSocket = socket;
    }

    @Override
    public byte[] readEventData() throws IOException {
        // parse packet header
        byte[] data;
        try {
            data = SocketHelper.getBuffer(replicationSocket, BINLOG_PACKET_HEADER_LENGTH);
        } catch (SocketTimeoutException e) {
            throw new NormalSocketTimeoutException(e);
        }
        PacketHeader header = ProtocolHelper.getProtocolHeader(data);

        // get event payload
        data = SocketHelper.getBuffer(replicationSocket, header.getPacketLength());
        return data;
    }

    @Override
    public boolean isOpenReplication() {
        return replicationSocket != null;
    }

    @Override
    public void close() {
        // 同时关闭复制链接和查询链接
        super.closeSocket(replicationSocket);
        replicationSocket = null;
    }

    @Override
    protected Socket createQuerySocket() {
        try {
            return updateSettings(getNewSocket());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载已经同步数据的位置,从该位置继续加载数据，防止重复
     *
     * @return BinlogAndOffsetSyncPoint
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    protected BinlogAndOffsetSyncPoint getBinlogFileAndPosition()
            throws IOException, NoSuchAlgorithmException {
        checkRowFormat();
        SyncPoint eventPosition = getEventPositionFromThreadLocalCache();
        if (eventPosition != null) {
            getLogger().info("Get valid start position from JVM TheadLocal:{}",
                    eventPosition);
            return extender.extend(eventPosition, this);
        }
        eventPosition = disposeEventPosition.loadSyncPoint();
        if (eventPosition != null) {
            getLogger().info("Get valid start position from local file:{}",
                    eventPosition);
            return extender.extend(eventPosition, this);
        }
        eventPosition = getConfigureStartPosition();
        if (eventPosition != null) {
            getLogger().info("Get valid start position from configuration xml or properties file:{}",
                    eventPosition);
            return extender.extend(eventPosition, this);
        }
        eventPosition = getMasterCurrentEventPosition();
        getLogger().info("Get valid start position from MySQL query status result:{}.",
                eventPosition);
        return extender.extend(eventPosition, this);
    }

    protected SyncPoint getEventPositionFromThreadLocalCache() {
        SyncPoint syncPoint = ThreadHolder.SYNC_POINT_CACHE.get();
        //TODO ad-hoc solution... better refactor later :-(
        /**
         * 针对MySQL Ares V5.1版本的逻辑，
         * 防止在不关心的增量下发时候，{@link AbstractProducer}内部会
         * 调用{@link AbstractProducer#endTrans(BaseLogEvent)}，
         * 来记录点，而一般点的都会wrap一个本地local存储的{@link net.neoremind.fountain.eventposition
         * .ReadonlyDisposeEventPosition}的delegate，但是threadlocal里面却记录了。
         * 导致切换数据源的时候这里会因此读取-1这个groupId的点。
         */
        if (syncPoint instanceof BaiduGroupIdSyncPoint &&
                ((BaiduGroupIdSyncPoint) syncPoint).getGroupId().equals(BigInteger.valueOf(-1))) {
            return null;
        }

        return syncPoint;
    }

    @Override
    public BinlogAndOffsetSyncPoint getMasterCurrentEventPosition()
            throws IOException {
        ResultSetPacket resultSetPacket = query("show master status");
        if (resultSetPacket == null) {
            throw new IOException("Can not query mysql master status");
        }

        List<RowValuePacket> rowValueList = resultSetPacket.getRowValueList();
        if (CollectionUtils.isEmpty(rowValueList)) {
            throw new DataErrorException(
                    "Query master status error, do not get row value");
        }

        RowValuePacket rowPacket = rowValueList.get(0);
        if (rowPacket == null
                || CollectionUtils.isEmpty(rowPacket.getFieldValueList())
                || rowPacket.getFieldValueList().size() < MASTER_STATUS_FIELD_MIN_CNT) {
            throw new DataErrorException(
                    "Query master status error, field format is not valid");
        }

        BinlogAndOffsetSyncPoint point = new BinlogAndOffsetSyncPoint();
        point.addSyncPoint(getIpAddress(), getPort(), rowPacket
                .getFieldValueList().get(0), BigInteger.valueOf(Long
                .parseLong(rowPacket.getFieldValueList().get(1))));

        return point;
    }

    /**
     * 检查mysql binlog的格式,用于原生的row base bin log
     *
     * @throws IOException
     */
    protected void checkRowFormat() throws IOException {
        ResultSetPacket resultSetPacket =
                query("show variables like 'binlog_format';");
        if (resultSetPacket == null) {
            throw new IOException("Can not query mysql binlog format");
        }

        List<RowValuePacket> rowValueList = resultSetPacket.getRowValueList();
        if (CollectionUtils.isEmpty(rowValueList) || rowValueList.size() != 1) {
            throw new DataErrorException("Query binlog format error");
        }

        RowValuePacket rowPacket = rowValueList.get(0);
        if (rowPacket == null
                || CollectionUtils.isEmpty(rowPacket.getFieldValueList())
                || rowPacket.getFieldValueList().size() != 2) {
            throw new DataErrorException("Query binlog format error");
        }

        boolean isRow =
                MysqlCommonConstants.RowFormat.BINLOG_FORMAT_ROW.getValue()
                        .equalsIgnoreCase(rowPacket.getFieldValueList().get(1));
        ;
        if (!isRow) {
            throw new DataErrorException(
                    "Mysql binlog format is not row-based!");
        }
    }

    public int getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    public void setSlaveIdGenerateStrategy(
            SlaveIdGenerateStrategy slaveIdGenerateStrategy) {
        this.slaveIdGenerateStrategy = slaveIdGenerateStrategy;
    }

    protected void resetSlaveId() {
        if (slaveIdGenerateStrategy != null) {
            int slaveId = (Integer) slaveIdGenerateStrategy.get();
            getLogger().info("slaveId={} will used to dump binlog since {} is enabled", slaveId,
                    slaveIdGenerateStrategy.getClass().getSimpleName());
            setSlaveId(slaveId);
        }
    }

    public DisposeEventPosition getDisposeEventPosition() {
        return disposeEventPosition;
    }

    public void setDisposeEventPosition(
            DisposeEventPosition disposeEventPosition) {
        this.disposeEventPosition = disposeEventPosition;
    }

    public EventPositionExtender getExtender() {
        return extender;
    }

    public void setExtender(EventPositionExtender extender) {
        this.extender = extender;
    }

    @Override
    public BinlogDumpStrategy getBinlogDumpStrategy() {
        return null;
    }

    @Override
    public void setBinlogDumpStrategy(BinlogDumpStrategy binlogDumpStrategy) {

    }
}
