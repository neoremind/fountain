package net.neoremind.fountain.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import net.neoremind.fountain.meta.TableMeta;
import net.neoremind.fountain.packet.OKPacket;
import net.neoremind.fountain.packet.ResultSetPacket;

/**
 * 抽象描述mysql的数据源,包括本地binlog, rowbase binlog,databus等
 *
 * @author zhangxu
 */
public interface MysqlDataSource {

    /**
     * 打开一个数据源,用以读取数据
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws TimeoutException
     */
    void open() throws IOException, NoSuchAlgorithmException, TimeoutException;

    /**
     * 数据源是否已经处于打开状态
     *
     * @return
     */
    boolean isOpen();

    /**
     * 关闭数据源
     */
    void close();

    /**
     * 执行修改数据的sql
     *
     * @param query
     *
     * @return
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    OKPacket update(String query) throws IOException, NoSuchAlgorithmException;

    /**
     * 执行查询sql
     *
     * @param query
     *
     * @return
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    ResultSetPacket query(String query) throws IOException, NoSuchAlgorithmException;

    /**
     * 获取当前数据源的ip地址
     *
     * @return
     */
    String getIpAddress();

    /**
     * 获取当前数据源的端口
     *
     * @return
     */
    int getPort();

    /**
     * 查询给定表的schema
     *
     * @param tableFullName
     *
     * @return
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    TableMeta queryTableMeta(String tableFullName) throws IOException, NoSuchAlgorithmException;
}
