package net.neoremind.fountain.support;

import java.util.Arrays;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.eventposition.GtId;
import net.neoremind.fountain.rowbaselog.event.FormatDescriptionEvent;
import net.neoremind.fountain.rowbaselog.event.GtidEvent;
import net.neoremind.fountain.rowbaselog.event.QueryLogEvent;
import net.neoremind.fountain.rowbaselog.event.RotateEvent;
import net.neoremind.fountain.rowbaselog.event.RowsLogEvent;
import net.neoremind.fountain.rowbaselog.event.RowsLogEventV1;
import net.neoremind.fountain.rowbaselog.event.RowsLogEventV2;
import net.neoremind.fountain.rowbaselog.event.XidLogEvent;

/**
 * 事务上下文，一般用于在接收binlog的主处理流程中保存一些上下文信息，例如包括：
 * <ul>
 * <li>当前正在处理事务的gtid</li>
 * <li>当前正在处理事务的binlog filename和position</li>
 * <li>binlog最先接到固定的一个{@link FormatDescriptionEvent}，包含MySQL的一些基本信息，例如版本</li>
 * </ul>
 *
 * @author zhangxu
 */
public class TrxContext {

    /**
     * sid，一般达标server UUID
     * <p/>
     * 仅在MySQL5.6版本中使用，server UUID的格式为：
     * <pre>
     *     3E11FA47-71CA-11E1-9E33-C80AA9429562
     * </pre>
     * 这是一个十六进制的数组标示，去掉符号“-”，剩下的就是这个字节数组，大小固定是16个byte
     *
     * @see <a href="http://dev.mysql.com/doc/refman/5.6/en/replication-gtids-concepts
     * .html">replication-gtids-concepts</a>
     */
    private byte[] sid;

    /**
     * 当前处理的gtid
     * <ul>
     * <li>对于百度的MySQL Ares5.1版本，代表groupId</li>
     * <li>对于MySQL5.6版本，代表gtid</li>
     * </ul>
     *
     * @see GtId
     */
    private Long currGtId;

    /**
     * 当前处理event的binlog名称，这个binlog名称一般是由{@link RotateEvent}中得到了，事实上，
     * MySQL server在binlog stream中最先推送的就是一个{@link RotateEvent}，我们从这个里面得到。
     * 当一个binlog大小到达上限，一般是服务端my.cnf配置的max-binlog-size时候，也会有这样一个event告知
     * 客户端下一个binglog的文件名。
     */
    private String binlogFileName;

    /**
     * 当前处理的event的最开始那个{@link QueryLogEvent}，也就是BEGIN的偏移量。<br/>
     * 这个值从{@link QueryLogEvent}包含BEGIN中的{@link net.neoremind.fountain.rowbaselog.event
     * .BinlogEventHeader}中得到。
     */
    private Long nextBinlogPosition;

    /**
     * 当前处理的event的下一个{@link QueryLogEvent}，也就是BEGIN的偏移量。<br/>
     * 这个值从{@link XidLogEvent}或者{@link QueryLogEvent}包含ROLLBACK中的{@link net.neoremind.fountain.rowbaselog.event
     * .BinlogEventHeader}中得到。
     */
    private Long currBinlogPosition;

    /**
     * binlog一开始接受到的{@link FormatDescriptionEvent}
     */
    private FormatDescriptionEvent fmtDescEvent;

    /**
     * MySQL server是否启用校验和
     * <p/>
     * 可以使用<code>show variables like 'binlog_checksum'</code>查看，目前就两个选项CRC32或者NONE。
     */
    private boolean isChecksumSupport;

    /**
     * 接受到binlog event后回调的event监听器，以上这些上下文的值都是通过监听器回调设置进去的
     */
    private EventListener eventListener;

    /**
     * 构造方法
     * <p/>
     * 新建{@link #eventListener}并初始化所有事件回调
     */
    private TrxContext() {
        eventListener = EventListener.factory();
        eventListener.addCallback(FormatDescriptionEvent.class, new FormatDescriptionEventCallback());
        eventListener.addCallback(RowsLogEvent.class, new RowsLogEventCallback());
        eventListener.addCallback(RowsLogEventV1.class, new RowsLogEventCallback());
        eventListener.addCallback(RowsLogEventV2.class, new RowsLogEventCallback());
        eventListener.addCallback(GtidEvent.class, new GtIdEventCallback());
        eventListener.addCallback(RotateEvent.class, new RotateEventCallback());
        eventListener.addCallback(QueryLogEvent.class, new QueryLogEventCallback());
        eventListener.addCallback(XidLogEvent.class, new XidLogEventCallback());
    }

    /**
     * 静态构造方法
     *
     * @return TrxContext
     */
    public static TrxContext factory() {
        return new TrxContext();
    }

    @Override
    public String toString() {
        return "TrxContext{" +
                "currGtId=" + currGtId +
                ", binlogFileName='" + binlogFileName + '\'' +
                ", preBinlogPosition=" + currBinlogPosition +
                ", nextBinlogPosition=" + nextBinlogPosition +
                ", fmtDescEvent=" + fmtDescEvent +
                ", sid=" + Arrays.toString(sid) +
                ", isChecksumSupport=" + isChecksumSupport +
                '}';
    }

    public void handleEvent(BaseLogEvent event) {
        eventListener.handle(event, this);
    }

    public byte[] getSid() {
        return sid;
    }

    public void setSid(byte[] sid) {
        this.sid = sid;
    }

    public FormatDescriptionEvent getFmtDescEvent() {
        return fmtDescEvent;
    }

    public void setFmtDescEvent(FormatDescriptionEvent fmtDescEvent) {
        this.fmtDescEvent = fmtDescEvent;
    }

    public Long getCurrGtId() {
        return currGtId;
    }

    public void setCurrGtId(Long currGtId) {
        this.currGtId = currGtId;
    }

    public String getBinlogFileName() {
        return binlogFileName;
    }

    public void setBinlogFileName(String binlogFileName) {
        this.binlogFileName = binlogFileName;
    }

    public Long getNextBinlogPosition() {
        return nextBinlogPosition;
    }

    public void setNextBinlogPosition(Long nextBinlogPosition) {
        this.nextBinlogPosition = nextBinlogPosition;
    }

    public Long getCurrBinlogPosition() {
        return currBinlogPosition;
    }

    public void setCurrBinlogPosition(Long currBinlogPosition) {
        this.currBinlogPosition = currBinlogPosition;
    }

    public boolean isChecksumSupport() {
        return isChecksumSupport;
    }

    public void setIsChecksumSupport(boolean isChecksumSupport) {
        this.isChecksumSupport = isChecksumSupport;
    }
}
