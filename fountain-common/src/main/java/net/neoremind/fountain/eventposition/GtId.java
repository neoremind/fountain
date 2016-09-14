package net.neoremind.fountain.eventposition;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import net.neoremind.fountain.exception.GtIdInvalidException;
import net.neoremind.fountain.util.StringPool;

/**
 * MySQL 5.6版本之后使用的Global Transaction Id，简称为GtId。<br/>
 * GtId由
 * <pre>
 *     GTID = source_id:transaction_id
 * </pre>
 * 组成，其中source_id由MySQL server决定，通常就是server的uuid；transaction_id是事务提交到MySQL server的顺序，从1开始编号，依次递增。
 * <p/>
 * 例如一个GtId如下：
 * <pre>
 *     3E11FA47-71CA-11E1-9E33-C80AA9429562:23
 * </pre>
 * 这里的GtId类是供{@link GtIdSet}使用的，GtId Set是一批GtId所组成的，用于主从复制，因此从会提交给主一个自己以及同步过的position，也就是可以理解为fountain中的{@link
 * SyncPoint}，因此需要有一个区段，包含开始和结束，也就是本类中使用的{@link #intervalStart}和{@link #intervalEnd}。
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/refman/5.6/en/replication-gtids-concepts.html">replication-gtids-concepts</a>
 */
public class GtId {

    /**
     * server uuid的正则表达式，例如cf716fda-74e2-11e2-b7b7-000c290a6b8f。
     */
    private static final String SERVER_UUID_REG =
            "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    /**
     * server uuid的正则表达式Pattern
     */
    private static final Pattern SERVER_UUID_PATTERN = Pattern.compile(SERVER_UUID_REG);

    /**
     * server uuid
     * <p/>
     * MySQL 5.6用128位的server_uuid代替了原本的32位server_id的大部分功能。
     * 原因很简单，server_id依赖于my.cnf的手工配置，有可能产生冲突 —— 而自动产生
     * 128位uuid的算法可以保证所有的MySQL uuid都不会冲突。
     * <p/>
     * 在首次启动时MySQL会调用generate_server_uuid()自动生成一个 server_uuid，并且保存到 auto.cnf 文件 —— 这个文件目前存在的唯一目的就是保存 server_uuid。
     * <p/>
     * 在MySQL再次启动时会读取 auto.cnf 文件，继续使用上次生成的 server_uuid。
     * <p/>
     * 使用SHOW命令可以查看MySQL实例当前使用的server_uuid​：SHOW GLOBAL VARIABLES LIKE 'server_uuid';
     */
    private String serverUUID;

    /**
     * 将20个byte的server uuid去掉“-”分隔符还原为16个byte的字节数组
     */
    private byte[] serverUUIDbyte;

    /**
     * gtid区段开始
     */
    private long intervalStart;

    /**
     * gtid区段结束
     */
    private long intervalEnd;

    /**
     * 默认构造方法
     */
    public GtId() {
    }

    /**
     * 构造方法
     *
     * @param serverUUID    server uuid
     * @param intervalStart gtid区段开始
     * @param intervalEnd   gtid区段结束
     */
    public GtId(String serverUUID, long intervalStart, long intervalEnd) {
        this.serverUUID = serverUUID;
        this.serverUUIDbyte = new BigInteger(serverUUID.replace("-", ""), 16).toByteArray();
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    /**
     * 从字符串还原成GtId类对象
     *
     * @param str 字符串输入，例如05b47d41-7b10-11e2-9fff-00241db92e69:1-19
     *
     * @return GtId类对象
     *
     * @throws GtIdInvalidException 当server uuid或者区段解析错误时候抛出的异常
     */
    public static GtId buildFromString(String str) throws GtIdInvalidException {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            String[] serverUUIDAndInterval = StringUtils.split(str, StringPool.Symbol.COLON);
            String serverUUID = serverUUIDAndInterval[0];
            Matcher serverUUIDfound = SERVER_UUID_PATTERN.matcher(serverUUID);
            if (!serverUUIDfound.find()) {
                throw new GtIdInvalidException("GtId server UUID:" + serverUUID + " does not match pattern");
            }
            String[] intevals = StringUtils.split(serverUUIDAndInterval[1], StringPool.Symbol.DASH);
            Long intervalStart = Long.parseLong(intevals[0]);
            Long intervalEnd = Long.parseLong(intevals[1]);
            if (intervalStart > intervalEnd) {
                throw new GtIdInvalidException("GtId interval start is greater thant end");
            }
            return new GtId(serverUUID, intervalStart, intervalEnd);
        } catch (NumberFormatException e) {
            throw new GtIdInvalidException("GtId intervals is not valid, " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new GtIdInvalidException("GtId intervals should have 2 pairs");
        }
    }

    /**
     * 按照05b47d41-7b10-11e2-9fff-00241db92e69:1-19这个格式打印
     *
     * @return 打印字符串
     */
    @Override
    public String toString() {
        return serverUUID + StringPool.Symbol.COLON + intervalStart + StringPool.Symbol.DASH + intervalEnd;
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public long getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(long intervalStart) {
        this.intervalStart = intervalStart;
    }

    public long getIntervalEnd() {
        return intervalEnd;
    }

    public void setIntervalEnd(long intervalEnd) {
        this.intervalEnd = intervalEnd;
    }

    public byte[] getServerUUIDbyte() {
        return serverUUIDbyte;
    }

    public void setServerUUIDbyte(byte[] serverUUIDbyte) {
        this.serverUUIDbyte = serverUUIDbyte;
    }
}
