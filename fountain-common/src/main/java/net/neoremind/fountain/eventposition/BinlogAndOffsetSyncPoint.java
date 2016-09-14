package net.neoremind.fountain.eventposition;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 描述传统row base binlog同步点
 *
 * @author hexiufeng
 */
public class BinlogAndOffsetSyncPoint implements SyncPoint {
    private static final String MULPT_SEP = ";";

    private List<MysqlSyncPoint> syncPointGroup = new ArrayList<MysqlSyncPoint>();

    /**
     * 无参数构造方法
     */
    public BinlogAndOffsetSyncPoint() {

    }

    /**
     * 有参数构造反复
     *
     * @param syncPointGroup MysqlSyncPoint list
     */
    public BinlogAndOffsetSyncPoint(List<MysqlSyncPoint> syncPointGroup) {
        this.syncPointGroup = syncPointGroup;
    }

    /**
     * 描述传统的mysql的同步点
     *
     * @author hexiufeng
     */
    public static class MysqlSyncPoint {
        public static final String INFO_SEP = "#";
        /**
         * host:port
         */
        private String mysqlId;
        private String binlogName;
        private BigInteger offset = BigInteger.valueOf(-1);

        public String getMysqlId() {
            return mysqlId;
        }

        public void setMysqlId(String mysqlId) {
            this.mysqlId = mysqlId;
        }

        public String getBinlogName() {
            return binlogName;
        }

        public void setBinlogName(String binlogName) {
            this.binlogName = binlogName;
        }

        public BigInteger getOffset() {
            return offset;
        }

        public void setOffset(BigInteger offset) {
            this.offset = offset;
        }

        @Override
        public String toString() {
            return mysqlId + INFO_SEP + binlogName + INFO_SEP + offset;
        }

        /**
         * 由string转化为对象
         *
         * @param info 同步点 string
         */
        public void fromString(String info) {
            String[] array = info.split("#");
            mysqlId = array[0];
            binlogName = array[1];
            offset = new BigInteger(array[2]);
        }
    }

    /**
     * 根据host或者ip和port获取MysqlSyncPoint对象，使用MysqlSyncPoint中的binlogname和offset来同步数据
     *
     * @param host host
     * @param ip   ip
     * @param port port
     *
     * @return MysqlSyncPoint
     */
    public MysqlSyncPoint getPointByHostAndPort(String host, String ip, int port) {
        MysqlSyncPoint ret = getPointByHostAndPort(host, port);
        if (ret == null) {
            return getPointByHostAndPort(ip, port);
        }
        return ret;
    }

    /**
     * 根据host和port获取MysqlSyncPoint对象，使用MysqlSyncPoint中的binlogname和offset来同步数据
     *
     * @param host host
     * @param port port
     *
     * @return MysqlSyncPoint
     */
    public MysqlSyncPoint getPointByHostAndPort(String host, int port) {
        String mysqlId = host + ":" + port;
        for (MysqlSyncPoint pt : this.syncPointGroup) {
            if (pt.getMysqlId().equals(mysqlId)) {
                return pt;
            }
        }
        return null;
    }

    /**
     * 增加sync point
     *
     * @param host       host
     * @param port       port
     * @param binlogName binlog file name
     * @param offset     binlog file内偏移地址
     */
    public void addSyncPoint(String host, int port, String binlogName, BigInteger offset) {
        MysqlSyncPoint myPoint = new MysqlSyncPoint();
        myPoint.setMysqlId(host + ":" + port);
        myPoint.setBinlogName(binlogName);
        myPoint.setOffset(offset);
        syncPointGroup.add(myPoint);
    }

    @Override
    public byte[] toBytes() {
        if (syncPointGroup == null || syncPointGroup.size() == 0) {
            return null;
        }
        return convert2String().getBytes();
    }

    @Override
    public String toString() {
        return convert2String();
    }

    /**
     * 把MysqlSyncPoint list转成字符串描述
     *
     * @return
     */
    private String convert2String() {
        if (syncPointGroup == null || syncPointGroup.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (MysqlSyncPoint pt : syncPointGroup) {
            sb.append(pt.toString());
            sb.append(MULPT_SEP);
        }
        return sb.substring(0, sb.length() - 1);
    }

    @Override
    public void parse(byte[] buf) {
        String info = new String(buf);
        info = StringUtils.chomp(info);
        String[] array = info.split(";");
        for (String val : array) {
            MysqlSyncPoint pt = new MysqlSyncPoint();
            pt.fromString(val);
            syncPointGroup.add(pt);
        }
    }

    public List<MysqlSyncPoint> getSyncPointGroup() {
        return Collections.unmodifiableList(syncPointGroup);
    }

}
