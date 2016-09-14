package net.neoremind.fountain.producer.datasource.binlogdump;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.GtIdSet;
import net.neoremind.fountain.eventposition.GtIdSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.packet.PacketHeader;
import net.neoremind.fountain.packet.ResultSetPacket;
import net.neoremind.fountain.packet.RowValuePacket;
import net.neoremind.fountain.producer.exception.ReplicationEventPositionInvalidException;
import net.neoremind.fountain.producer.packet.BinLogDumpCommandPacket;
import net.neoremind.fountain.producer.packet.BinLogDumpGtidCommandPacket;
import net.neoremind.fountain.util.CollectionUtils;
import net.neoremind.fountain.util.SocketHelper;

/**
 * 做binlog dump的辅助类
 *
 * @author zhangxu
 */
public class BinlogDumpSupport {

    /**
     * 发送传统的根据binlog filename+position做binlog dump的命令
     *
     * @param syncPoint         同步点
     * @param replicationSocket 复制线程
     * @param slaveId           fountain的slaveId
     *
     * @throws IOException
     */
    public static void dumpBinlog(SyncPoint syncPoint, Socket replicationSocket, int slaveId) throws IOException {
        BinLogDumpCommandPacket binlogDumpPacket = new BinLogDumpCommandPacket();
        if (!(syncPoint instanceof BinlogAndOffsetSyncPoint)) {
            throw new ReplicationEventPositionInvalidException(
                    "SyncPoint should be a type of BinlogAndOffsetSyncPoint");
        }
        BinlogAndOffsetSyncPoint.MysqlSyncPoint mySqlPoint =
                ((BinlogAndOffsetSyncPoint) syncPoint).getPointByHostAndPort(
                        replicationSocket.getInetAddress().getHostName(),
                        replicationSocket.getInetAddress().getHostAddress(), replicationSocket.getPort());
        binlogDumpPacket.setBinlogFileName(mySqlPoint.getBinlogName());
        binlogDumpPacket.setBinlogPos(mySqlPoint.getOffset().intValue());
        binlogDumpPacket.setServerId(slaveId);
        sendDumpPacket(replicationSocket, binlogDumpPacket.toBytes());
    }

    /**
     * 发送MySQL5.6之后支持的GTID做binlog dump的命令
     *
     * @param syncPoint         同步点
     * @param replicationSocket 复制线程
     * @param slaveId           fountain的slaveId
     *
     * @throws IOException
     */
    public static void dumpBinlogGtId(SyncPoint syncPoint, Socket replicationSocket, int slaveId) throws IOException {
        if (!(syncPoint instanceof GtIdSyncPoint)) {
            throw new ReplicationEventPositionInvalidException(
                    "SyncPoint should be a type of GtIdSyncPoint");
        }
        BinLogDumpGtidCommandPacket binlogDumpPacket = new BinLogDumpGtidCommandPacket();
        binlogDumpPacket.setGtIdSet(((GtIdSyncPoint) syncPoint).getGtIdSet());
        sendDumpPacket(replicationSocket, binlogDumpPacket.toBytes());
    }

    /**
     * 发送packet
     *
     * @param replicationSocket 复制线程
     * @param dumpPacketBody    dump的byte数组
     *
     * @throws IOException
     */
    private static void sendDumpPacket(Socket replicationSocket, byte[] dumpPacketBody) throws IOException {
        PacketHeader header = new PacketHeader();
        header.setPacketLength(dumpPacketBody.length);
        header.setPacketNumber((byte) 0x00);

        SocketHelper.writeByte(replicationSocket, header.toBytes());
        SocketHelper.writeByte(replicationSocket, dumpPacketBody);
    }

    /**
     * 通过<tt>show master status</tt>查询获取同步点
     *
     * @param dataSource 数据源
     *
     * @return 同步点
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static SyncPoint getMasterCurrentBinlogFileNameAndPosition(MysqlDataSource dataSource)
            throws IOException, NoSuchAlgorithmException {
        ResultSetPacket resultSetPacket = dataSource.query("show master status");
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
                || rowPacket.getFieldValueList().size() < 4) {
            throw new DataErrorException(
                    "Query master status error, field format is not valid");
        }

        BinlogAndOffsetSyncPoint point = new BinlogAndOffsetSyncPoint();
        point.addSyncPoint(dataSource.getIpAddress(), dataSource.getPort(), rowPacket
                .getFieldValueList().get(0), BigInteger.valueOf(Long
                .parseLong(rowPacket.getFieldValueList().get(1))));

        return point;
    }

    /**
     * 在MySQL5.6版本中，通过<tt>show global variables like '%gtid_executed%'</tt>获取同步点
     *
     * @param dataSource 数据源
     *
     * @return 同步点
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static SyncPoint getMasterCurrentExecutedGtIdSet(MysqlDataSource dataSource)
            throws IOException, NoSuchAlgorithmException {
        ResultSetPacket resultSetPacket = dataSource.query("show global variables like '%gtid_executed%'");
        if (resultSetPacket == null) {
            throw new IOException("Can not query mysql master gtid_executed");
        }

        List<RowValuePacket> rowValueList = resultSetPacket.getRowValueList();
        if (CollectionUtils.isEmpty(rowValueList)) {
            throw new DataErrorException(
                    "Query master gtid_executed error, do not get row value");
        }

        RowValuePacket rowPacket = rowValueList.get(0);
        if (rowPacket == null
                || CollectionUtils.isEmpty(rowPacket.getFieldValueList())
                || rowPacket.getFieldValueList().size() != 2) {
            throw new DataErrorException(
                    "Query master gtid_executed error, field format is not valid");
        }

        if (StringUtils.isEmpty(rowPacket.getFieldValueList().get(1))) {
            throw new DataErrorException(
                    "Query master gtid_executed error, gtid_executed is empty");
        }

        return new GtIdSyncPoint(GtIdSet.buildFromString(rowPacket.getFieldValueList().get(1)));
    }

}
