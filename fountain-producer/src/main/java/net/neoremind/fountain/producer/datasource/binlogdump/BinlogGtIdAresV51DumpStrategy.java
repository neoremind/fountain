package net.neoremind.fountain.producer.datasource.binlogdump;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.BaiduGroupIdSyncPoint;
import net.neoremind.fountain.eventposition.EventPositionExtender;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.producer.datasource.eventpositionext.GtId2BinPositionEventPositionExtender;
import net.neoremind.fountain.producer.exception.ReplicationEventPositionInvalidException;
import net.neoremind.fountain.producer.exception.UnsupportedBinlogDumpException;

/**
 * 百度MySQL Ares 5.1版本的binlog dump策略
 *
 * @author zhangxu
 */
public class BinlogGtIdAresV51DumpStrategy extends AbstractBinlogDumpStrategy implements BinlogDumpStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BinlogGtIdAresV51DumpStrategy.class);

    /**
     * 百度MySQL的gtid
     */
    private Long gtId;

    /**
     * 同步点扩展器。
     * <p/>
     * 从一个{@link SyncPoint}转为另外一个同步点的转换方法，用于同步点不能直接利用来发送binlog dump命令，而是需要转换下。
     * <p/>
     * 针对百度MySQL Ares版本需要将记录的groupid的同步点，转换为binlogfile+position类型的同步点。
     */
    private EventPositionExtender extender = new GtId2BinPositionEventPositionExtender();

    @Override
    public boolean isSupport(MysqlDataSource dataSource) throws UnsupportedBinlogDumpException {
        return doIsSupport(dataSource, new MySQLVersionValidationCallback() {
            @Override
            public String getCheckFieldRegex() {
                return "5.1.*";
            }
        }, new BinlogRowFormatValidationCallback());
    }

    @Override
    public void dumpBinlog(SyncPoint syncPoint, Socket replicationSocket, int slaveId) throws IOException {
        BinlogDumpSupport.dumpBinlog(syncPoint, replicationSocket, slaveId);
    }

    @Override
    public void logInfo() {
        if (gtId != null && gtId > 0) {
            logger.info("gtId " + gtId + " would be used to dump binlog if possible");
        }
    }

    @Override
    public SyncPoint getConfiguredPosition(MysqlDataSource dataSource) {
        if (gtId == null || gtId < 0) {
            return null;
        }
        BaiduGroupIdSyncPoint baiduGroupIdSyncPoint = new BaiduGroupIdSyncPoint(BigInteger.valueOf(gtId.longValue()));
        return baiduGroupIdSyncPoint;
    }

    @Override
    public SyncPoint getMasterCurrentPosition(MysqlDataSource dataSource) throws IOException, NoSuchAlgorithmException {
        return BinlogDumpSupport.getMasterCurrentBinlogFileNameAndPosition(dataSource);
    }

    @Override
    public SyncPoint transformSyncPoint(SyncPoint syncPoint, MysqlDataSource dataSource)
            throws IOException, NoSuchAlgorithmException {
        if (extender != null) {
            return extender.extend(syncPoint, dataSource);
        }
        return super.transformSyncPoint(syncPoint, dataSource);
    }

    @Override
    public SyncPoint createCurrentSyncPoint(MysqlDataSource dataSources, byte[] sid, Long gtId, String binlogFileName,
                                            Long binlogPosition) {
        if (gtId == null) {
            logger.debug("Try to create syncpoint but GtId is null, maybe the table is not your interest and GtId "
                    + "can only be obtained from RowsLogEvent...");
            return new BaiduGroupIdSyncPoint(BigInteger.valueOf(-1));
        }
        return new BaiduGroupIdSyncPoint(BigInteger.valueOf(gtId));
    }

    @Override
    public void applySyncPoint(MysqlDataSource dataSources, SyncPoint syncPoint) {
        if (!(syncPoint instanceof BaiduGroupIdSyncPoint)) {
            throw new ReplicationEventPositionInvalidException(
                    "SyncPoint should be a type of BaiduGroupIdSyncPoint");
        }
        BaiduGroupIdSyncPoint baiduGroupIdSyncPoint = (BaiduGroupIdSyncPoint) syncPoint;
        this.gtId = baiduGroupIdSyncPoint.getGroupId().longValue();
    }

    public Long getGtId() {
        return gtId;
    }

    public void setGtId(Long gtId) {
        this.gtId = gtId;
    }
}
