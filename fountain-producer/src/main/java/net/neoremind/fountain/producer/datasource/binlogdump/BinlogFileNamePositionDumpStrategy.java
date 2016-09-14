package net.neoremind.fountain.producer.datasource.binlogdump;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.producer.exception.ReplicationEventPositionInvalidException;
import net.neoremind.fountain.producer.exception.UnsupportedBinlogDumpException;

/**
 * 传统的基于MySQL binlogfilename + position做binlog dump的策略
 *
 * @author zhangxu
 */
public class BinlogFileNamePositionDumpStrategy extends AbstractBinlogDumpStrategy implements BinlogDumpStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BinlogFileNamePositionDumpStrategy.class);

    /**
     * binlog文件名
     */
    private String binlogFileName;

    /**
     * binlog偏移量
     */
    private Long binlogPosition;

    @Override
    public boolean isSupport(MysqlDataSource dataSource) throws UnsupportedBinlogDumpException {
        return doIsSupport(dataSource, new MySQLVersionValidationCallback() {
            @Override
            public String getCheckFieldRegex() {
                return "5.*";
            }
        }, new BinlogRowFormatValidationCallback());
    }

    @Override
    public void dumpBinlog(SyncPoint syncPoint, Socket replicationSocket, int slaveId) throws IOException {
        BinlogDumpSupport.dumpBinlog(syncPoint, replicationSocket, slaveId);
    }

    @Override
    public SyncPoint getConfiguredPosition(MysqlDataSource dataSource) {
        if (StringUtils.isEmpty(binlogFileName) || binlogPosition == null || binlogPosition < 1) {
            return null;
        }
        BinlogAndOffsetSyncPoint binlogAndOffsetSyncPoint = new BinlogAndOffsetSyncPoint();
        binlogAndOffsetSyncPoint.addSyncPoint(
                dataSource.getIpAddress(),
                dataSource.getPort(),
                binlogFileName,
                BigInteger.valueOf(binlogPosition));
        return binlogAndOffsetSyncPoint;
    }

    @Override
    public SyncPoint getMasterCurrentPosition(MysqlDataSource dataSource) throws IOException, NoSuchAlgorithmException {
        return BinlogDumpSupport.getMasterCurrentBinlogFileNameAndPosition(dataSource);
    }

    @Override
    public void logInfo() {
        if (!StringUtils.isEmpty(binlogFileName) && binlogPosition > 0) {
            logger.info("binlogFileName and position will be used to dump binlog");
            logger.info("binlogFileName is " + binlogFileName);
            logger.info("binlogPosition is " + binlogPosition);
        }
    }

    @Override
    public SyncPoint createCurrentSyncPoint(MysqlDataSource dataSources, byte[] sid, Long gtId, String binlogFileName,
                                            Long binlogPosition) {
        BinlogAndOffsetSyncPoint point = new BinlogAndOffsetSyncPoint();
        point.addSyncPoint(dataSources.getIpAddress(), dataSources.getPort(), binlogFileName, BigInteger.valueOf(
                binlogPosition));
        return point;
    }

    @Override
    public void applySyncPoint(MysqlDataSource dataSources, SyncPoint syncPoint) {
        if (!(syncPoint instanceof BinlogAndOffsetSyncPoint)) {
            throw new ReplicationEventPositionInvalidException(
                    "SyncPoint should be a type of BinlogAndOffsetSyncPoint");
        }
        BinlogAndOffsetSyncPoint binlogAndOffsetSyncPoint = (BinlogAndOffsetSyncPoint) syncPoint;
        BinlogAndOffsetSyncPoint.MysqlSyncPoint mysqlSyncPoint =
                binlogAndOffsetSyncPoint.getPointByHostAndPort(dataSources.getIpAddress(), dataSources.getPort());
        if (mysqlSyncPoint == null) {
            throw new ReplicationEventPositionInvalidException(
                    "BinlogAndOffsetSyncPoint cannot find by " + dataSources.getIpAddress() + ":" + dataSources
                            .getPort());
        }
        this.binlogFileName = mysqlSyncPoint.getBinlogName();
        this.binlogPosition = mysqlSyncPoint.getOffset().longValue();
    }

    public String getBinlogFileName() {
        return binlogFileName;
    }

    public void setBinlogFileName(String binlogFileName) {
        this.binlogFileName = binlogFileName;
    }

    public Long getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(Long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

}
