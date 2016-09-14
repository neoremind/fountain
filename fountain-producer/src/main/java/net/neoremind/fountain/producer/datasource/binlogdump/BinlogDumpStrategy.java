package net.neoremind.fountain.producer.datasource.binlogdump;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.GtId;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.producer.exception.UnsupportedBinlogDumpException;
import net.neoremind.fountain.producer.datasource.BinlogDataSource;
import net.neoremind.fountain.rowbaselog.event.RotateEvent;
import net.neoremind.fountain.rowbaselog.event.RowsLogEvent;

/**
 * 当从库连接到主库以后，从库向主库发送一条dump命令，开始复制过程。<br/>
 * binlog dump策略接口，子类泛化成为具体的实施办法，主要用于屏蔽{@link BinlogDataSource}向MySQL
 * server发送binlog dump命令的各种方案，做到开闭原则，对修改封闭，对扩展开放。
 * <p/>
 * 一般一个{@link net.neoremind.fountain.producer.datasource
 * .BinlogDataSource}对应一个策略，因为一般来说虽然逻辑上是一个同步点，但是binlogfilename+position可能不尽相同。
 * <p/>
 * 目前需要支持：
 * <ul>
 * <li>1）传统的根据binlog filename+position的方案</li>
 * <li>2）百度MySQL中需要根据groupid反查binlog filename+position，然后再进行dump</li>
 * <li>3）MySQL5.6之后支持的GTID方案</li>
 * </ul>
 *
 * @author zhangxu
 */
public interface BinlogDumpStrategy {

    /**
     * 该种binlog dump策略是否在当前的MySQL版本下支持，不支持则抛出UnsupportedBinlogDumpException异常
     * <p/>
     * 例如如果是MySQL5.1，那么就不能使用通过gtid set dump binlog的策略，类似的校验逻辑
     *
     * @param dataSource 数据源
     *
     * @return 是否支持
     *
     * @throws UnsupportedBinlogDumpException
     */
    boolean isSupport(MysqlDataSource dataSource) throws UnsupportedBinlogDumpException;

    /**
     * 通过复制socket发送binlog dump命令给MySQL Server
     *
     * @param syncPoint         欲同步的点
     * @param replicationSocket 复制的socket
     * @param slaveId           fountain的slaveId，对于MySQL server来说没有个注册或者查询的客户端都需要这个slave id
     *
     * @throws IOException
     */
    void dumpBinlog(SyncPoint syncPoint, Socket replicationSocket, int slaveId) throws IOException;

    /**
     * 打印该dump策略的一些信息
     */
    void logInfo();

    /**
     * 是否支持校验和，MySQL server如果开启了checksum，则这里要和MySQL server一致。
     * <p/>
     * 一般来说如果启用了checksum，则每个{@link RowsLogEvent}后面都会多出来4个byte，
     * 以及{@link RotateEvent}中的binlog filename也会多出4个byte。
     *
     * @return
     */
    boolean isChecksumSupport();

    /**
     * 从一个{@link SyncPoint}转为另外一个同步点的转换方法，用于同步点不能直接利用来发送binlog dump命令，而是需要转换下。
     * <p/>
     * 一般情况不需要使用，主要针对百度MySQL Ares版本需要将记录的groupid的同步点，
     * 转换为binlogfile+position类型的同步点。
     *
     * @param syncPoint  同步点
     * @param dataSource 数据源
     *
     * @return 转换后的同步点
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    SyncPoint transformSyncPoint(SyncPoint syncPoint, MysqlDataSource dataSource)
            throws IOException, NoSuchAlgorithmException;

    /**
     * 获取配置文件中的同步点，一般是子类指定的一些属性变量
     *
     * @param dataSource 数据源
     *
     * @return 同步点
     */
    SyncPoint getConfiguredPosition(MysqlDataSource dataSource);

    /**
     * 直接查询MySQL Server获取最实时的同步点
     *
     * @param dataSource 数据源
     *
     * @return 同步点
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    SyncPoint getMasterCurrentPosition(MysqlDataSource dataSource) throws IOException, NoSuchAlgorithmException;

    /**
     * 根据运行时的信息（可以包括gtid，binlog filename以及position）构建事务中的同步点，用于新建{@link SyncPoint}，
     * 这个同步点可以在producer中保存持久化到本地，同时也会加入到{@link net.neoremind.fountain.changedata
     * .ChangeDataSet}中下发到内部的消费者，内部消费者可以拿到这个同步点，也可以选择持久化。
     *
     * @param dataSources    数据源
     * @param sid            服务器的server UUID，可以参考{@link GtId#serverUUIDbyte}
     * @param gtId           事务中的gtid，对于官方MySQL 5.1来说，不会存在这个
     * @param binlogFileName 当前binlog filename
     * @param binlogPosition 当前binlog position offset
     *
     * @return 同步点
     */
    SyncPoint createCurrentSyncPoint(MysqlDataSource dataSources, byte[] sid, Long gtId, String binlogFileName,
                                     Long binlogPosition);

    /**
     * 通过SyncPoint填充策略属性
     *
     * @param dataSources 数据源
     * @param syncPoint   同步点
     */
    void applySyncPoint(MysqlDataSource dataSources, SyncPoint syncPoint);

}