package net.neoremind.fountain.datasource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.exception.DataErrorException;
import net.neoremind.fountain.exception.DataSourceInvalidException;
import net.neoremind.fountain.meta.ColumnMeta;
import net.neoremind.fountain.meta.TableMeta;
import net.neoremind.fountain.packet.ClientAuthPacket;
import net.neoremind.fountain.packet.EOFPacket;
import net.neoremind.fountain.packet.ErrorPacket;
import net.neoremind.fountain.packet.FieldDescriptionPacket;
import net.neoremind.fountain.packet.HandshakePacket;
import net.neoremind.fountain.packet.OKPacket;
import net.neoremind.fountain.packet.PacketHeader;
import net.neoremind.fountain.packet.QueryCommandPacket;
import net.neoremind.fountain.packet.ResultSetHeaderPacket;
import net.neoremind.fountain.packet.ResultSetPacket;
import net.neoremind.fountain.packet.RowValuePacket;
import net.neoremind.fountain.util.CollectionUtils;
import net.neoremind.fountain.util.MysqlCommonConstants;
import net.neoremind.fountain.util.ProtocolHelper;
import net.neoremind.fountain.util.SocketHelper;

/**
 * 数据源的抽象实现,实现大部分的通用的功能,比如打开连接、权限验证等,建议其他实现MysqlDataSource接口的类都继承本类
 *
 * @author zhangxu
 */
public abstract class AbstractMysqlDataSource implements MysqlDataSource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMysqlDataSource.class);
    /**
     * 数据源基础配置
     */
    protected DatasourceConfigure conf = new DatasourceConfigure();

    /**
     * 获取当前实现的logger
     *
     * @return
     */
    protected abstract Logger getLogger();

    /**
     * 创建一个用于查询的socket
     */
    protected abstract Socket createQuerySocket();

    /**
     * 使用给定的socket作为当前实现的主socket
     *
     * @param socket
     */
    protected abstract void applySocket(Socket socket);

    @Override
    public void open() throws IOException, NoSuchAlgorithmException, TimeoutException {

        // 打印mysql相关参数
        printMysqlInfo();

        if (isOpen()) {
            getLogger().warn("dataSource is already open");
            return;
        }

        applySocket(updateSettings(getNewSocket()));
        getLogger().warn("Open socket stream to MySQL server done");
    }

    @Override
    public OKPacket update(String query) throws IOException {
        getLogger().info("Update sql: " + query);
        Socket querySocket = createQuerySocket();
        try {
            return update(querySocket, query);
        } finally {
            IOUtils.closeQuietly(querySocket);
        }
    }

    @Override
    public ResultSetPacket query(String query) throws IOException {
        getLogger().info("Query sql: " + query);
        Socket querySocket = createQuerySocket();
        try {
            return query(querySocket, query);
        } finally {
            IOUtils.closeQuietly(querySocket);
        }
    }

    /**
     * 封装mysql query text协议，实现查询功能
     *
     * @param socket query socket stream
     * @param query  query语句
     *
     * @return ResultSetPacket
     *
     * @throws IOException
     * @see <a href="http://dev.mysql.com/doc/internals/en/com-query-response.html">COM_QUERY Response</a>
     */
    private ResultSetPacket query(Socket socket, String query) throws IOException {

        if (socket == null || query == null) {
            throw new DataSourceInvalidException("dataSource is not open or query is null");
        }

        byte[] data = sendRequestAndGetResponse(socket, query);

        // 结果1 result set header，主要获取字段数量
        ResultSetHeaderPacket resultSetHeaderPacket = new ResultSetHeaderPacket();
        resultSetHeaderPacket.fromBytes(data);

        // 结果2 field packet，遍历字段数量，依次读取解析成FieldDescriptionPacket
        List<FieldDescriptionPacket> fieldPacketList = new ArrayList<FieldDescriptionPacket>();
        for (int i = 0; i < resultSetHeaderPacket.getColumnCount(); i++) {
            data = readPacket(socket);

            FieldDescriptionPacket fieldPacket = new FieldDescriptionPacket();
            fieldPacket.fromBytes(data);

            fieldPacketList.add(fieldPacket);
        }

        // 结果3 eof packet
        readEofPacket(socket);

        // 结果4 row packet
        List<RowValuePacket> rowDataPacketList = new ArrayList<RowValuePacket>();
        while (true) {
            data = readPacket(socket);
            if (data[0] == -2) {
                break;
            }
            RowValuePacket rowDataPacket = new RowValuePacket();
            rowDataPacket.fromBytes(data);
            rowDataPacketList.add(rowDataPacket);
        }

        ResultSetPacket resultSet = new ResultSetPacket();
        resultSet.getFieldDescriptionList().addAll(fieldPacketList);
        for (RowValuePacket row : rowDataPacketList) {
            resultSet.getRowValueList().add(row);
        }

        return resultSet;
    }

    /**
     * 调用如下的命令获取列信息，保存为{@link ColumnMeta}列表返回：
     * <pre>
     * mysql> show full fields from City;
     * +------------+----------+------+-----+---------+----------------+
     * | Field      | Type     | Null | Key | Default | Extra          |
     * +------------+----------+------+-----+---------+----------------+
     * | Id         | int(11)  | NO   | PRI | NULL    | auto_increment |
     * | Name       | char(35) | NO   |     |         |                |
     * | Country    | char(3)  | NO   | UNI |         |                |
     * | District   | char(20) | YES  | MUL |         |                |
     * | Population | int(11)  | NO   |     | 0       |                |
     * +------------+----------+------+-----+---------+----------------+
     * 5 rows in set (0.00 sec)
     * </pre>
     *
     * @param tableFullName 表全名
     *
     * @return ColumnMeta列表
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */

    @Override
    public TableMeta queryTableMeta(String tableFullName) throws IOException, NoSuchAlgorithmException {
        // 调用mysql desc xxx sql
        ResultSetPacket resultSetPacket = query("show full fields from " + tableFullName);
        if (resultSetPacket == null) {
            throw new IOException("Can not query table meta");
        }

        List<FieldDescriptionPacket> fieldDescriptionList = resultSetPacket.getFieldDescriptionList();
        List<RowValuePacket> rowValueList = resultSetPacket.getRowValueList();
        if (CollectionUtils.isEmpty(fieldDescriptionList) || CollectionUtils.isEmpty(rowValueList)) {
            throw new DataErrorException("Query table meta error");
        }

        TableMeta tableMeta = new TableMeta();
        tableMeta.setFullName(tableFullName);

        List<ColumnMeta> columnMetaList = new ArrayList<ColumnMeta>();
        tableMeta.setColumnMetaList(columnMetaList);

        for (RowValuePacket rowPacket : rowValueList) {
            List<String> fieldValueList = rowPacket.getFieldValueList();
            if (CollectionUtils.isEmpty(fieldValueList) || fieldValueList.size() != 9) {
                logger.warn("do not match desc table's desc");
                continue;
            }

            ColumnMeta columnMeta = new ColumnMeta();
            columnMetaList.add(columnMeta);

            columnMeta.setColumnName(fieldValueList.get(0));
            columnMeta.setColumnType(fieldValueList.get(1));
            columnMeta.setCharset(fieldValueList.get(2));
            columnMeta.setNullFlag(fieldValueList.get(3));
            columnMeta.setKeyFlag(fieldValueList.get(4));
            columnMeta.setDefaultValue(fieldValueList.get(5));
            columnMeta.setExtra(fieldValueList.get(6));
        }

        return tableMeta;
    }

    @Override
    public String getIpAddress() {
        return conf.getMysqlServer();
    }

    @Override
    public int getPort() {
        return conf.getMysqlPort();
    }

    /**
     * 安全的关闭socket连接，使用apache commons io组件实现
     *
     * @param socket
     */
    protected void closeSocket(Socket socket) {
        if (socket != null) {
            logClose(socket);
            IOUtils.closeQuietly(socket);
        }
    }

    /**
     * 打印当前mysql的信息
     *
     * @throws IOException
     */
    protected void printMysqlInfo() throws IOException {
        getLogger().info("----- print mysql info ");
        getLogger().info(conf.toString());
    }

    /**
     * 创建一个新的socket连接，完成mysql握手和权限验证，并且后续建立network stream，后续使用
     *
     * @return 和服务端的Socket连接
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws TimeoutException
     */
    protected Socket getNewSocket() throws IOException, NoSuchAlgorithmException, TimeoutException {
        // 发起连接
        Socket socket = new Socket();
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setSoTimeout(conf.getSoTimeout());
        socket.setTcpNoDelay(true);
        socket.setReceiveBufferSize(conf.getReceiveBufferSize());
        socket.setSendBufferSize(conf.getSendBufferSize());

        InetSocketAddress address = new InetSocketAddress(conf.getMysqlServer(), conf.getMysqlPort());
        try {
            socket.connect(address, conf.getConnectTimeout());
        } catch (SocketTimeoutException e) {
            IOUtils.closeQuietly(socket);
            throw e;
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);
            throw e;
        }

        // 连接到mysql
        HandshakePacket handshakePacket = handshake(socket);

        // 进行身份验证
        clientAuthorise(socket, handshakePacket);

        return socket;
    }

    /**
     * 封装mysql query text协议，实现update语句的执行
     *
     * @param socket
     * @param query
     *
     * @return
     *
     * @throws IOException
     */
    protected OKPacket update(Socket socket, String query) throws IOException {

        if (socket == null || query == null) {
            throw new DataSourceInvalidException("dataSource is not open or query is null");
        }

        byte[] data = sendRequestAndGetResponse(socket, query);

        OKPacket packet = new OKPacket();
        packet.fromBytes(data);

        return packet;
    }

    /**
     * 对新构建的mysql 连接设置配置信息：
     * <ul>
     * <li>wait_timeout</li>
     * <li>net_write_timeout</li>
     * <li>net_read_timeout</li>
     * <li>charset</li>
     * </ul>
     *
     * @param socket
     *
     * @return
     *
     * @throws IOException
     */
    protected Socket updateSettings(Socket socket) throws IOException {
        getLogger().debug("update MySQL socket params...");
        try {
            getLogger().debug("set wait_timeout = " + conf.getWaitTimeout());
            update(socket, "set wait_timeout=" + conf.getWaitTimeout());
        } catch (Exception e) {
            getLogger().warn(null, e);
        }
        try {
            getLogger().debug("set net_write_timeout = " + conf.getNetWriteTimeout());
            update(socket, "set net_write_timeout=" + conf.getNetWriteTimeout());
        } catch (Exception e) {
            getLogger().warn(null, e);
        }

        try {
            getLogger().debug("set net_read_timeout = " + conf.getNetReadTimeout());
            update(socket, "set net_read_timeout=" + conf.getNetReadTimeout());
        } catch (Exception e) {
            getLogger().warn(null, e);
        }

        try {
            // 设置服务端返回结果时不做编码转化，直接按照数据库的二进制编码进行发送，由客户端自己根据需求进行编码转化
            getLogger().debug("set charset = " + conf.getCharset());
            update(socket, "set names '" + conf.getCharset() + "'");
        } catch (Exception e) {
            getLogger().warn(null, e);
        }
        return socket;
    }

    /**
     * 记录关闭socket的日志
     *
     * @param socket
     */
    private void logClose(Socket socket) {
        StringBuffer sb =
                new StringBuffer("Close MySQL datasource, [ip, port] is [").append(socket.getInetAddress())
                        .append(", ").append(socket.getPort()).append("].");
        getLogger().info(sb.toString());
    }

    /**
     * 完成mysql连接的握手协议
     *
     * @param socket
     *
     * @return
     *
     * @throws IOException
     */
    private HandshakePacket handshake(Socket socket) throws IOException {

        // 接受欢迎信息
        byte[] data = SocketHelper.getBuffer(socket, 4);
        PacketHeader header = ProtocolHelper.getProtocolHeader(data);

        data = SocketHelper.getBuffer(socket, header.getPacketLength());
        if (data == null || data.length <= 0) {
            throw new DataErrorException("data is null or empty");
        }
        if (data[0] == -1 || data[0] == -2) {
            ErrorPacket errorPacket = new ErrorPacket();
            errorPacket.fromBytes(data);
            throw new DataErrorException(
                    "Receive Error Packet! first byte is " + data[0] + ", detail is " + errorPacket.toString());
        }

        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.fromBytes(data);

        return handshakePacket;
    }

    /**
     * 实现mysql的权限验证协议
     *
     * @param socket
     * @param handshakePacket
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void clientAuthorise(Socket socket, HandshakePacket handshakePacket) throws IOException,
            NoSuchAlgorithmException {
        // 发送验证信息
        ClientAuthPacket clientAuthPacket = new ClientAuthPacket();
        clientAuthPacket.setUsername(conf.getUserName());
        clientAuthPacket.setPassword(conf.getPassword());
        clientAuthPacket.setDatabaseName(conf.getDatabaseName());
        clientAuthPacket.setCharsetNumber(MysqlCommonConstants.CLIENT_CHARSET_NUMBER);
        clientAuthPacket.setScrumbleBuff(handshakePacket.getScrambleBuff());

        byte[] clientAuthPacketBody = clientAuthPacket.toBytes();
        PacketHeader header = new PacketHeader();
        header.setPacketLength(clientAuthPacketBody.length);
        header.setPacketNumber((byte) (header.getPacketNumber() + 1));

        SocketHelper.writeByte(socket, header.toBytes());
        SocketHelper.writeByte(socket, clientAuthPacketBody);

        // 接受验证通过信息
        byte[] data = SocketHelper.getBuffer(socket, 4);
        header = ProtocolHelper.getProtocolHeader(data);
        data = SocketHelper.getBuffer(socket, header.getPacketLength());
        if ((data[0] & 0xff) == 0xff) {
            ErrorPacket errorPacket = new ErrorPacket();
            errorPacket.fromBytes(data);
            throw new IOException("Error When doing Client Authentication:" + errorPacket.getErrorCode() + ", "
                    + errorPacket.getMessage());
        } else if ((data[0] & 0xff) == 0xfe) {
            EOFPacket eof = new EOFPacket();
            eof.fromBytes(data);
            throw new IOException("Eof When doing Client Authentication:" + eof.getEofFlag());
        }

    }

    /**
     * 使用文本协议<a href="http://dev.mysql.com/doc/internals/en/text-protocol.html">Text
     * Protocol</a>中的<a href="http://dev.mysql.com/doc/internals/en/com-query
     * .html">COM_QUERY</a>进行堵塞模式下发生mysql请求并读取返回信息。
     * <p/>
     * Payload如下：
     * <pre>
     * 1              [03] COM_QUERY
     * string[EOF]    the query the server shall execute
     * </pre>
     * 返回的信息是返回Packet的Payload字节数组
     *
     * @param socket query socket stream
     * @param query  query sql语句
     *
     * @return Packet的Payload字节数组，可能是{@link OKPacket}或者ResultSet，详细参考<a href="http://dev.mysql
     * .com/doc/internals/en/com-query-response.html#packet-COM_QUERY_Response">packet-COM_QUERY_Response</a>
     *
     * @throws IOException 当返回的Packet Payload第一个字节小于0x00，则有可能是{@link ErrorPacket}
     */
    private byte[] sendRequestAndGetResponse(Socket socket, String query) throws IOException {

        // 构造查询
        QueryCommandPacket queryPacket = new QueryCommandPacket();
        queryPacket.setSql(query);

        // 发送请求
        byte[] queryPacketBody = queryPacket.toBytes();
        PacketHeader header = new PacketHeader();
        header.setPacketLength(queryPacketBody.length);
        header.setPacketNumber((byte) 0);

        SocketHelper.writeByte(socket, header.toBytes());
        SocketHelper.writeByte(socket, queryPacketBody);

        // 接受结果
        byte[] data = readPacket(socket);
        if (data[0] < 0) {
            ErrorPacket errorPacket = new ErrorPacket();
            errorPacket.fromBytes(data);
            throw new IOException("error when execute sql of [" + query + "], error code is :"
                    + errorPacket.getErrorCode() + ", " + errorPacket.getMessage());
        }

        return data;
    }

    /**
     * 从msyql连接中读取信息
     *
     * @param socket
     *
     * @return
     *
     * @throws IOException
     */
    private byte[] readPacket(Socket socket) throws IOException {
        byte[] data = SocketHelper.getBuffer(socket, 4);
        PacketHeader header = ProtocolHelper.getProtocolHeader(data);
        data = SocketHelper.getBuffer(socket, header.getPacketLength());
        return data;
    }

    /**
     * 从mysql连接中读取Eof信息
     *
     * @param socket
     *
     * @throws IOException
     */
    private void readEofPacket(Socket socket) throws IOException {
        byte[] eofBody = readPacket(socket);
        if (eofBody[0] != -2) {
            throw new IOException("EOF Packet is expected, but packet with field_count=" + eofBody[0] + " is found.");
        }
    }

    public DatasourceConfigure getConf() {
        return conf;
    }

    public void setConf(DatasourceConfigure conf) {
        this.conf = conf;
    }

}
