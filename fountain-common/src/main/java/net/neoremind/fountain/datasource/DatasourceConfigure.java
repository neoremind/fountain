package net.neoremind.fountain.datasource;

/**
 * 数据源的配置
 *
 * @author zhangxu
 */
public class DatasourceConfigure {

    /**
     * Mysql IP
     */
    private String mysqlServer;

    /**
     * Mysql 端口
     */
    private int mysqlPort;

    /**
     * Mysql 用户名
     */
    private String userName;

    /**
     * Mysql 密码
     */
    private String password;

    /**
     * Mysql 数据库名称
     */
    private String databaseName;

    /**
     * Mysql replication socket的so timeout，默认300s=5min。
     * <p/>
     * 一旦超时，会自动切换另外一个数据源，fountain保证多数据源之间的HA
     */
    private int soTimeout = 300 * 1000;

    /**
     * Mysql replication socket滑动窗口读缓冲区大小，默认16k
     */
    private int receiveBufferSize = 16 * 1024;

    /**
     * Mysql replication socket滑动窗口写缓冲区大小，默认16k
     */
    private int sendBufferSize = 16 * 1024;

    /**
     * Mysql replication socket连接超时，默认3s
     */
    private int connectTimeout = 3000;

    /**
     * 执行MySQL命令<code>set wait_timeout = xxx</code>使用，默认很长，约等于115天。
     * The number of seconds the server waits for activity on a noninteractive connection before closing it.
     */
    private int waitTimeout = 9999999;

    /**
     * 执行MySQL命令<code>set net_write_timeout = xxx</code>使用，默认240s=4min。
     * 这个参数只对TCP/IP链接有效，
     * 是数据库发送网络包给客户端的超时时间。
     */
    private int netWriteTimeout = 240;

    /**
     * 执行MySQL命令<code>set net_read_timeout = xxx</code>使用，默认240s=4min。
     * 这个参数只对TCP/IP链接有效，
     * 是数据库等待接收客户端发送网络包的超时时间。
     */
    private int netReadTimeout = 240;

    /**
     * MySQL字符集
     */
    private String charset = "binary";

    @Override
    public String toString() {
        return "mysqlServer=" + mysqlServer +
                ", mysqlPort=" + mysqlPort +
                ", userName=" + userName +
                ", password=" + password +
                ", databaseName='" + databaseName +
                ", soTimeout=" + soTimeout +
                ", receiveBufferSize=" + receiveBufferSize +
                ", sendBufferSize=" + sendBufferSize +
                ", connectTimeout=" + connectTimeout +
                ", waitTimeout=" + waitTimeout +
                ", netWriteTimeout=" + netWriteTimeout +
                ", netReadTimeout=" + netReadTimeout +
                ", charset='" + charset;
    }

    public String getMysqlServer() {
        return mysqlServer;
    }

    public void setMysqlServer(String mysqlServer) {
        this.mysqlServer = mysqlServer;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public void setMysqlPort(int mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public int getNetWriteTimeout() {
        return netWriteTimeout;
    }

    public void setNetWriteTimeout(int netWriteTimeout) {
        this.netWriteTimeout = netWriteTimeout;
    }

    public int getNetReadTimeout() {
        return netReadTimeout;
    }

    public void setNetReadTimeout(int netReadTimeout) {
        this.netReadTimeout = netReadTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
