package net.neoremind.fountain.eventposition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.neoremind.fountain.exception.GtIdInvalidException;
import net.neoremind.fountain.util.ProtocolHelper;
import net.neoremind.fountain.util.StringPool;

/**
 * MySQL 5.6之后引入Gtid_set数据结构，目的是为了轻量级的记录大量的连续全局事务ID，
 * 比如已经在从数据库上执行的全局事务ID, 或者主库上正在运行的全局事务ID。这些集合往往包含海量的全局事务ID。
 * <p/>
 * 在连续的情况下，可以在常数时间内判断一个GtID是否包含在集合内。
 * 这很容易用来检查一个GtID是否已经执行过，因此可以用在防止MySQL事务在同一个Slave上重复执行这类场景。
 * <p/>
 * Gtid_set的结构是一个以sidno，通常为server uuid，为序号的数组，每个数组元素都指向一条Interval组成的链表，链表中的每个Interval
 * 用来存放一组事务ID的区段，例如 （1，5）。
 * <pre>
 * Gtid_set
 * | sidno: 1 | -> Interval (1, 5)   -> Interval (11, 18)
 * | sidno: 2 | -> Interval (1, 27) -> Interval (29, 115) -> Interval (117, 129)
 * | sidno: 3 | -> Interval (1, 154)
 * </pre>
 * 可以用如下命令看主库已经执行过的GtIdSet集合：show global variables like '%gtid_executed%'\G
 * <pre>
 * ************************** 1. row ***************************
 * Variable_name: gtid_executed
 * Value: 10a27632-a909-11e2-8bc7-0010184e9e08:1-4,
 * 153c0406-a909-11e2-8bc7-0010184e9e08:1-3,
 * 7a07cd08-ac1b-11e2-9fcf-0010184e9e08:1-31
 * </pre>
 * 这个数据结构在发送com-binlog-dump-gtid命令时候会用到，主要用于构建从库已经执行过的gtid集合，主库就不发送增量下来了。
 *
 * @author zhangxu
 * @see GtId
 * @see <a href="http://dev.mysql.com/doc/internals/en/com-binlog-dump-gtid.html">com-binlog-dump-gtid</a>
 */
public class GtIdSet {

    /**
     * GtID集合
     */
    private List<GtId> gtIdList = new ArrayList<GtId>(4);

    /**
     * 从字符串还原GtIdSet类对象
     *
     * @param str 例如05b47d41-7b10-11e2-9fff-00241db92e69:1-19;4f65e570-7b32-11e2-a0de-00241db92e69:22-66
     *
     * @return GtIdSet类对象
     */
    public static GtIdSet buildFromString(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        GtIdSet gtIdSet = new GtIdSet();
        String[] gtIdStrs = StringUtils.split(str, StringPool.Symbol.SEMICOLON);
        for (String gtIdStr : gtIdStrs) {
            gtIdSet.getGtIdList().add(GtId.buildFromString(gtIdStr));
        }
        return gtIdSet;
    }

    /**
     * 按照05b47d41-7b10-11e2-9fff-00241db92e69:1-19;4f65e570-7b32-11e2-a0de-00241db92e69:1这个格式打印
     *
     * @return 打印字符串
     */
    @Override
    public String toString() {
        return StringUtils.join(gtIdList, StringPool.Symbol.SEMICOLON);
    }

    /**
     * 递增gtid
     * <p/>
     * gtid要大于等于interval end，否则抛出GtIdInvalidException异常
     *
     * @param sid  server uuid 16byte array
     * @param gtId 当前处理的gtid
     *
     * @throws GtIdInvalidException
     */
    public void addGtId(byte[] sid, Long gtId) throws GtIdInvalidException {
        for (GtId id : gtIdList) {
            if (Arrays.equals(id.getServerUUIDbyte(), sid)) {
                if (gtId < id.getIntervalStart()) {
                    throw new GtIdInvalidException(
                            String.format(
                                    "This should never happen. Current gtId %s is less than interval start position %s",
                                    gtId, id.getIntervalStart()));
                }
                if (gtId < id.getIntervalEnd()) {
                    throw new GtIdInvalidException(
                            String.format(
                                    "This should never happen. Current gtId %s should greater than interval end "
                                            + "position %s",
                                    gtId, id.getIntervalEnd()));
                }
                id.setIntervalEnd(gtId);
            }
        }
    }

    /**
     * SID block的长度，按照这个计算：
     * 注意，这里只支持1个interval的情况。
     * <pre>
     * 8  n_sids
     * for n_sids {
     *     string[16]       SID
     *     8                n_intervals
     *     for n_intervals {
     *         8                start (signed)
     *         8                end (signed)
     *    }
     * }
     * </pre>
     *
     * @return 整体长度
     *
     * @see <a href="http://dev.mysql.com/doc/internals/en/com-binlog-dump-gtid.html">com-binlog-dump-gtid</a>
     */
    public int length() {
        return 8 + ((16 + 8 + 8 + 8) * gtIdList.size());
    }

    /**
     * 从库把自己的gtid_executed发送给主，这个数据结构封装了写socket stream的格式。
     * 注意，这里只支持1个interval的情况。
     * <p/>
     * 官网的格式如下：
     * <pre>
     * 8  n_sids
     * for n_sids {
     *     string[16]       SID
     *     8                n_intervals
     *     for n_intervals {
     *         8                start (signed)
     *         8                end (signed)
     *    }
     * }
     * </pre>
     * 解释如下：
     * <pre>
     * # n_sid           ulong  8bytes  == which size is the gtid_set
     * # | sid           uuid   16bytes UUID as a binary
     * # | n_intervals   ulong  8bytes  == how many intervals are sent for this gtid
     * # | | start       ulong  8bytes  Start position of this interval
     * # | | stop        ulong  8bytes  Stop position of this interval
     * </pre>
     *
     * @return
     */
    public void encode(ByteArrayOutputStream out) throws IOException {
        ProtocolHelper.writeUnsignedLongByLittleEndian(gtIdList.size(), out);
        for (GtId gtId : gtIdList) {
            out.write(gtId.getServerUUIDbyte());
            ProtocolHelper.writeUnsignedLongByLittleEndian(1, out);
            ProtocolHelper.writeUnsignedLongByLittleEndian(gtId.getIntervalStart(), out);
            ProtocolHelper.writeUnsignedLongByLittleEndian(gtId.getIntervalEnd(), out);
        }
    }

    public List<GtId> getGtIdList() {
        return gtIdList;
    }

    public void setGtIdList(List<GtId> gtIdList) {
        this.gtIdList = gtIdList;
    }

}
