package net.neoremind.fountain.rowbaselog.event;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.util.MysqlValueHelper;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * GTID event <br/>
 * 对于MySQL5.6.5及之后的版本，如果GTID_MODE = ON, 每个事务的replicate都会前置一个type code={@link EventConstant#GTID_EVENT}的事件。
 * 这个event用于标示GTID(Global Transaction Identifiers)即全局事务标识。
 *
 * @author zhangxu
 * @see <a href="http://dev.mysql.com/doc/internals/en/binlog-event-type.html">binlog-event-type</a>
 * @since 2015-12
 */
public class GtidEvent extends BaseLogEvent {

    private static final Logger logger = LoggerFactory.getLogger(GtidEvent.class);

    private static final long serialVersionUID = 3904701082535625594L;

    /**
     * MySQL Master的server uuid
     * <p/>
     * 目前是16个byte。例如：31a25a80-eee5-11e4-9dfd-90a380967173，按照16进制解析，去掉“-”，例如31代表0x31。
     * <p/>
     * MySQL Master上的查看的SQL语句如下：
     * <pre>
     * mysql> show global variables like '%uuid%';
     * +---------------+--------------------------------------+
     * | Variable_name | Value                                |
     * +---------------+--------------------------------------+
     * | server_uuid   | 31a25a80-eee5-11e4-9dfd-90a380967173 |
     * +---------------+--------------------------------------+
     * </pre>
     */
    private byte[] sid;

    /**
     * GTID(Global Transaction Identifiers)即全局事务标识，是MySQL Master基于事务粒度生成的一个递增的id。
     * <p/>
     * 当使用GTIDS时，在主库上提交的每一个事务都会被识别和跟踪，并且运用到所有从MySQL，而且配置主从或者主从切换时不再需要指定
     * binlog_filename和binlog_position。
     */
    private long gtId;

    /**
     * 构造方法
     *
     * @param eventHeader event header
     */
    public GtidEvent(BinlogEventHeader eventHeader) {
        super(eventHeader);
    }

    @Override
    public BaseLogEvent parseData(ByteBuffer buf) {
        byte[] flag = new byte[1];
        buf.get(flag);
        sid = MysqlValueHelper.getFixedBytes(buf, 16);
        gtId = UnsignedNumberHelper.convertLittleEndianLong(buf, 8);
        return this;
    }

    public byte[] getSid() {
        return sid;
    }

    public void setSid(byte[] sid) {
        this.sid = sid;
    }

    public long getGtId() {
        return gtId;
    }

    public void setGtId(long gtId) {
        this.gtId = gtId;
    }
}
