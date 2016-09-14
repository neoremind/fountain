package net.neoremind.fountain.producer.datasource;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategy;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategyAware;
import net.neoremind.fountain.support.ThreadHolder;

/**
 * 基于MySQL row based binlog实现的复制数据源
 * <p/>
 * 目前可以用于MySQL 5.1,5.6官方和自有的各种版本。
 *
 * @author zhangxu
 */
public class MysqlBinlogDataSource extends AbstractMysqlBinlogDataSource
        implements BinlogDataSource, BinlogDumpStrategyAware {

    private static final Logger logger = LoggerFactory.getLogger(MysqlBinlogDataSource.class);

    /**
     * 当从库连接到主库以后，从库向主库发送一条dump命令，开始复制过程。<br/>
     * binlog dump策略接口，子类泛化成为具体的实施办法，主要用于屏蔽{@link BinlogDataSource}向MySQL
     * server发送binlog dump命令的各种方案。
     */
    private BinlogDumpStrategy binlogDumpStrategy;

    @Override
    protected void printMysqlInfo() throws IOException {
        super.printMysqlInfo();
        getLogger().info("----- print binlog mysql info ");
        binlogDumpStrategy.logInfo();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void startReplication() throws IOException, NoSuchAlgorithmException, TimeoutException {
        logger.info("Start replication...");
        logger.info("Pre-checking...");
        binlogDumpStrategy.isSupport(this); // 检查MySQL server
        logger.info("Pre-checking done");
        resetSlaveId(); // 设置slaveId
        SyncPoint syncPoint = getBinlogDumpPosition(); // 获取同步点
        binlogDumpStrategy.dumpBinlog(syncPoint, replicationSocket, slaveId); // 发送DUMP命令给MySQL server
        logger.info("Send binlog dump command done, ready to process binlog events");
    }

    @Override
    protected SyncPoint getConfigureStartPosition() {
        return binlogDumpStrategy.getConfiguredPosition(this);
    }

    @Override
    public void apply(SyncPoint syncPoint) {
        if (binlogDumpStrategy == null) {
            throw new InternalError("binlog dump strategy should not be null");
        }

    }

    @Override
    protected Socket updateSettings(Socket socket) throws IOException {
        super.updateSettings(socket);
        if (binlogDumpStrategy.isChecksumSupport()) {
            try {
                getLogger().info("set @master_binlog_checksum= @@global.binlog_checksum");
                update(socket, "set @master_binlog_checksum= @@global.binlog_checksum");
            } catch (Exception e) {
                getLogger().warn(null, e);
            }
        }
        return socket;
    }

    /**
     * 加载已经同步数据的位置，从该位置继续加载数据，防止重复。
     * <p/>
     * 步骤如下：<br/>
     * 1）从{@link ThreadHolder#SYNC_POINT_CACHE}类中获取SyncPoint
     * <p/>
     * 2）从文件中获取byte[]，文件名规则为：
     * 所配置的rootPath + “/” + producerName [ + “.” + ext 有扩展名则加上]
     * 新建一个SyncPoint，使用parse()方法填充内容。
     * Parse()的不同实现如下：
     * <ul>
     * <li>同步点类	格式</li>
     * <li>BaiduGroupIdSyncPoint	Gtid（类型bigint）</li>
     * <li>GtIdSyncPoint	Gtid（类型bigint）</li>
     * <li>BaiduDatabusGroupIdSyncPoint	EventServerId + “:” + groupId</li>
     * <li>BinlogAndOffsetSyncPoint	MysqlId#binglogName#gtid <br/>
     * 例如： <br/>
     * 10.100.68.23:8309#mysql-bin.000005#87170457 <br/>
     * </li>
     * </ul>
     * <p/>
     * 3）从配置中获取SyncPoint，配置就是{@link BinlogDumpStrategy}中的，通常来自于XML中的配置值
     * <p/>
     * 4）获取MySQL Server最新的同步点
     * <p/>
     * 上述步骤中任意一次同步点不为空，则首先尝试转换下，然后再返回。
     *
     * @return SyncPoint
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    protected SyncPoint getBinlogDumpPosition()
            throws IOException, NoSuchAlgorithmException {
        SyncPoint eventPosition = super.getEventPositionFromThreadLocalCache();
        if (eventPosition != null) {
            getLogger().info("Get valid start position from JVM TheadLocal:{}",
                    eventPosition);
            return binlogDumpStrategy.transformSyncPoint(eventPosition, this);
        }
        eventPosition = disposeEventPosition.loadSyncPoint();
        if (eventPosition != null) {
            getLogger().info("Get valid start position from persistent storage(file or remote server):{}",
                    eventPosition);
            return binlogDumpStrategy.transformSyncPoint(eventPosition, this);
        }
        eventPosition = binlogDumpStrategy.getConfiguredPosition(this);
        if (eventPosition != null) {
            getLogger().info("Get valid start position from configuration xml or properties file:{}",
                    eventPosition);
            return binlogDumpStrategy.transformSyncPoint(eventPosition, this);
        }
        eventPosition = binlogDumpStrategy.getMasterCurrentPosition(this);
        getLogger().info("Get valid start position from MySQL query status result:{}.",
                eventPosition);
        return binlogDumpStrategy.transformSyncPoint(eventPosition, this);
    }

    @Override
    public BinlogDumpStrategy getBinlogDumpStrategy() {
        return binlogDumpStrategy;
    }

    @Override
    public void setBinlogDumpStrategy(
            BinlogDumpStrategy binlogDumpStrategy) {
        this.binlogDumpStrategy = binlogDumpStrategy;
    }
}
