package net.neoremind.fountain;

import net.neoremind.haguard.HaGuard;
import net.neoremind.fountain.datasource.DatasourceChoosePolicy;
import net.neoremind.fountain.datasource.DatasourceConfigure;
import net.neoremind.fountain.producer.dispatch.transcontrol.TransactionPolicy;
import net.neoremind.fountain.producer.matcher.TableMatcher;

/**
 * binlog同步器builder的公共模板父类
 *
 * @author zhangxu
 */
public abstract class BinlogSyncBuilderTemplate implements Builder<BinlogSyncer> {

    /**
     * 事务处理策略，在某些情况下需要控制事务中下发数据的数据。
     * <p/>
     * 比如：需要整事务下发处理时有些事务会比较大，这个大事务中的某些数据很多但是无用的， 需要丢弃这部分数据.
     *
     * @see TransactionPolicy
     */
    private TransactionPolicy transactionPolicy;

    /**
     * 黑名单表
     *
     * @see TableMatcher
     */
    private String blackTables;

    /**
     * 白名单表
     *
     * @see TableMatcher
     */
    private String whiteTables;

    /**
     * 增量消息消费者
     *
     * @see EventConsumer
     */
    private EventConsumer consumer;

    /**
     * Mysql 数据库名称
     *
     * @see DatasourceConfigure#databaseName
     */
    private String databaseName;

    /**
     * Mysql replication socket的so timeout，默认300s=5min。
     * <p/>
     * 一旦超时，会自动切换另外一个数据源，fountain保证多数据源之间的HA
     *
     * @see DatasourceConfigure#soTimeout
     */
    private int soTimeout;

    /**
     * Mysql replication socket滑动窗口读缓冲区大小，默认16k
     *
     * @see DatasourceConfigure#receiveBufferSize
     */
    private int receiveBufferSize;

    /**
     * Mysql replication socket滑动窗口写缓冲区大小，默认16k
     *
     * @see DatasourceConfigure#sendBufferSize
     */
    private int sendBufferSize;

    /**
     * Mysql replication socket连接超时，默认3s
     *
     * @see DatasourceConfigure#connectTimeout
     */
    private int connectTimeout;

    /**
     * 执行MySQL命令<code>set wait_timeout = xxx</code>使用，默认很长，约等于115天。
     * The number of seconds the server waits for activity on a noninteractive connection before closing it.
     *
     * @see DatasourceConfigure#waitTimeout
     */
    private int waitTimeout;

    /**
     * 执行MySQL命令<code>set net_write_timeout = xxx</code>使用，默认240s=4min。
     * 这个参数只对TCP/IP链接有效，
     * 是数据库发送网络包给客户端的超时时间。
     *
     * @see DatasourceConfigure#netWriteTimeout
     */
    private int netWriteTimeout;

    /**
     * 执行MySQL命令<code>set net_read_timeout = xxx</code>使用，默认240s=4min。
     * 这个参数只对TCP/IP链接有效，
     * 是数据库等待接收客户端发送网络包的超时时间。
     *
     * @see DatasourceConfigure#netReadTimeout
     */
    private int netReadTimeout;

    /**
     * MySQL字符集
     *
     * @see DatasourceConfigure#charset
     */
    private String charset;

    /**
     * 数据源选择策略，在ha场景中,选择备选datasource的策略
     *
     * @see DatasourceChoosePolicy
     */
    private DatasourceChoosePolicy datasourceChoosePolicy;

    /**
     * 生产者和消费者之间的内存缓存队列大小
     */
    private Integer messageQueueSize;

    /**
     * 高可用的guard
     */
    private HaGuard haGuard;

    /**
     * 默认构造方法
     */
    protected BinlogSyncBuilderTemplate() {

    }

    public BinlogSyncBuilderTemplate transactionPolicy(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
        return this;
    }

    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public String getBlackTables() {
        return blackTables;
    }

    public BinlogSyncBuilderTemplate blackTables(String blackTables) {
        this.blackTables = blackTables;
        return this;
    }

    public String getWhiteTables() {
        return whiteTables;
    }

    public BinlogSyncBuilderTemplate whiteTables(String whiteTables) {
        this.whiteTables = whiteTables;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public BinlogSyncBuilderTemplate databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public BinlogSyncBuilderTemplate soTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public BinlogSyncBuilderTemplate receiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public BinlogSyncBuilderTemplate sendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public BinlogSyncBuilderTemplate setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public BinlogSyncBuilderTemplate waitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
        return this;
    }

    public int getNetWriteTimeout() {
        return netWriteTimeout;
    }

    public BinlogSyncBuilderTemplate netWriteTimeout(int netWriteTimeout) {
        this.netWriteTimeout = netWriteTimeout;
        return this;
    }

    public int getNetReadTimeout() {
        return netReadTimeout;
    }

    public BinlogSyncBuilderTemplate netReadTimeout(int netReadTimeout) {
        this.netReadTimeout = netReadTimeout;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public BinlogSyncBuilderTemplate charset(String charset) {
        this.charset = charset;
        return this;
    }

    public DatasourceChoosePolicy getDatasourceChoosePolicy() {
        return datasourceChoosePolicy;
    }

    public BinlogSyncBuilderTemplate datasourceChoosePolicy(DatasourceChoosePolicy datasourceChoosePolicy) {
        this.datasourceChoosePolicy = datasourceChoosePolicy;
        return this;
    }

    public Integer getMessageQueueSize() {
        return messageQueueSize;
    }

    public BinlogSyncBuilderTemplate messageQueueSize(Integer messageQueueSize) {
        this.messageQueueSize = messageQueueSize;
        return this;
    }

    public EventConsumer getConsumer() {
        return consumer;
    }

    public BinlogSyncBuilderTemplate consumer(EventConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public HaGuard getHaGuard() {
        return haGuard;
    }

    public BinlogSyncBuilderTemplate haGuard(HaGuard haGuard) {
        this.haGuard = haGuard;
        return this;
    }
}
