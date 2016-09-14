package net.neoremind.fountain.producer.datasource.binlogdump;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.GtIdSet;
import net.neoremind.fountain.eventposition.GtIdSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.producer.exception.ReplicationEventPositionInvalidException;
import net.neoremind.fountain.producer.exception.UnsupportedBinlogDumpException;

/**
 * MySQL 5.6版本的binlog dump策略
 *
 * @author zhangxu
 */
public class BinlogGtIdV56DumpStrategy extends AbstractBinlogDumpStrategy implements BinlogDumpStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BinlogGtIdV56DumpStrategy.class);

    /**
     * gtid set
     * <p/>
     * 格式为sid:start-end,sid:start-end...
     * <p/>
     * 例如，
     * cf716fda-74e2-11e2-b7b7-000c290a6b8f:1-20<br/>
     * cf716fda-74e2-11e2-b7b7-000c290a6b8f:1-20,3E11FA47-71CA-11E1-9E33-C80AA9429562:27-566
     */
    private String gtIdset;

    /**
     * 内部使用的当前的gtid set
     *
     * @see GtIdSet
     */
    private GtIdSet currentGtIdSet;

    @Override
    public boolean isSupport(MysqlDataSource dataSource) throws UnsupportedBinlogDumpException {
        return doIsSupport(dataSource, new MySQLVersionValidationCallback() {
            @Override
            public String getCheckFieldRegex() {
                return "5.6.*";
            }
        }, new BinlogRowFormatValidationCallback(), new GtIdModeValidationCallback());
    }

    @Override
    public void dumpBinlog(SyncPoint syncPoint, Socket replicationSocket, int slaveId) throws IOException {
        if (!(syncPoint instanceof GtIdSyncPoint)) {
            throw new ReplicationEventPositionInvalidException(
                    "SyncPoint should be a type of GtIdSyncPoint");
        }
        currentGtIdSet = ((GtIdSyncPoint) syncPoint).getGtIdSet();
        BinlogDumpSupport.dumpBinlogGtId(syncPoint, replicationSocket, slaveId);
    }

    @Override
    public void logInfo() {
        if (gtIdset != null) {
            logger.info("GtIdSet " + gtIdset + " will be used to dump binlog");
        }
    }

    @Override
    public SyncPoint getConfiguredPosition(MysqlDataSource dataSource) {
        GtIdSet gtIdSet = GtIdSet.buildFromString(gtIdset);
        if (gtIdSet == null) {
            return null;
        }
        return new GtIdSyncPoint(gtIdSet);
    }

    @Override
    public SyncPoint getMasterCurrentPosition(MysqlDataSource dataSource) throws IOException, NoSuchAlgorithmException {
        return BinlogDumpSupport.getMasterCurrentExecutedGtIdSet(dataSource);
    }

    @Override
    public boolean isChecksumSupport() {
        this.isChecksumSupport = true;
        return super.isChecksumSupport();
    }

    @Override
    public SyncPoint createCurrentSyncPoint(MysqlDataSource dataSources, byte[] sid, Long gtId, String binlogFileName,
                                            Long binlogPosition) {
        currentGtIdSet.addGtId(sid, gtId);
        return new GtIdSyncPoint(currentGtIdSet);
    }

    @Override
    public void applySyncPoint(MysqlDataSource dataSources, SyncPoint syncPoint) {
        if (!(syncPoint instanceof GtIdSyncPoint)) {
            throw new ReplicationEventPositionInvalidException(
                    "SyncPoint should be a type of GtIdSyncPoint");
        }
        GtIdSyncPoint gtIdSyncPoint = (GtIdSyncPoint) syncPoint;
        this.gtIdset = gtIdSyncPoint.getGtIdSet().toString();
    }

    public String getGtIdset() {
        return gtIdset;
    }

    public void setGtIdset(String gtIdset) {
        this.gtIdset = gtIdset;
    }
}
