package net.neoremind.fountain.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

import net.neoremind.fountain.exception.DataSourceInvalidException;
import net.neoremind.fountain.meta.TableMeta;
import net.neoremind.fountain.packet.OKPacket;
import net.neoremind.fountain.packet.ResultSetPacket;
import net.neoremind.fountain.util.CollectionUtils;

/**
 * mysql ha 支持的数据源抽象。该实现使用组装模式，实现{@link MysqlDataSource MysqlDataSource}接口。
 * 支持泛型，mysqlbinlog、databus数据源的ha都可以基于此类实现。
 * <p/>
 * 该ha数据源包含多个{@link MysqlDataSource MysqlDataSource}，使用
 * {@link DatasourceChoosePolicy DatasourceChoosePolicy}来选择数据源，初始化或者当前数据源断开时都可以 使用
 * {@link DatasourceChoosePolicy DatasourceChoosePolicy}选择的新的数据源。
 *
 * @author zhangxu
 */
public abstract class AbstractHADatasource<T extends MysqlDataSource> implements MysqlDataSource {
    /**
     * 备选的数据源列表
     */
    protected List<T> mysqlDataSourceList;
    /**
     * 记录当前选中的数据源
     */
    protected volatile T currentDataSource;
    /**
     * 数据源选择策略
     */
    protected DatasourceChoosePolicy datasourceChoosePolicy;

    public List<T> getMysqlDataSourceList() {
        return mysqlDataSourceList;
    }

    public void setMysqlDataSourceList(List<T> mysqlDataSourceList) {
        this.mysqlDataSourceList = mysqlDataSourceList;
    }

    public DatasourceChoosePolicy getDatasourceChoosePolicy() {
        return datasourceChoosePolicy;
    }

    public void setDatasourceChoosePolicy(DatasourceChoosePolicy datasourceChoosePolicy) {
        this.datasourceChoosePolicy = datasourceChoosePolicy;
    }

    /**
     * 在ha的环境中封装一批类似的业务逻辑,比如执行sql，查询表的元数据等，本身是一种策略模式，被用于模板方法中
     *
     * @param command
     * @param method
     * @param taskExecutor
     *
     * @return
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    protected abstract <E> E doHaTask(final String command, String method, TaskExcutor<E> taskExecutor)
            throws NoSuchAlgorithmException, IOException;

    /**
     * 初始化选中的数据源
     *
     * @param choosedDattasource
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws TimeoutException
     */
    protected abstract void prepareChoosedDatasouce(T choosedDattasource) throws IOException, NoSuchAlgorithmException,
            TimeoutException;

    /**
     * 获取当前的logger对象
     *
     * @return
     */
    protected abstract Logger getLogger();

    @Override
    public void open() throws IOException, NoSuchAlgorithmException, TimeoutException {
        checkDataSourceListEmpty();
        chooseMysqlDataSource();
    }

    @Override
    public boolean isOpen() {
        return currentDataSource != null && currentDataSource.isOpen();
    }

    @Override
    public void close() {
        getLogger().info("Try to close HA MysqlDataSource ...");
        if (mysqlDataSourceList == null) {
            return;
        }
        for (MysqlDataSource dataSource : mysqlDataSourceList) {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    @Override
    public String getIpAddress() {
        return currentDataSource == null ? null : currentDataSource.getIpAddress();
    }

    @Override
    public int getPort() {
        return currentDataSource == null ? 0 : currentDataSource.getPort();
    }

    @Override
    public OKPacket update(String query) throws IOException, NoSuchAlgorithmException {
        return doHaTask(query, "update", new TaskExcutor<OKPacket>() {

            @Override
            public OKPacket execute(String command, MysqlDataSource dataSouce) throws Exception {
                return dataSouce.update(command);
            }

        });
    }

    @Override
    public ResultSetPacket query(String query) throws IOException, NoSuchAlgorithmException {
        return doHaTask(query, "query", new TaskExcutor<ResultSetPacket>() {

            @Override
            public ResultSetPacket execute(String command, MysqlDataSource dataSouce) throws Exception {
                return dataSouce.query(command);
            }

        });
    }

    @Override
    public TableMeta queryTableMeta(String tableFullName) throws IOException, NoSuchAlgorithmException {
        return doHaTask(tableFullName, "queryTableMeta", new TaskExcutor<TableMeta>() {

            @Override
            public TableMeta execute(String command, MysqlDataSource dataSouce) throws Exception {
                return dataSouce.queryTableMeta(command);
            }

        });
    }

    /**
     * 检查备选数据源是否为空，如果为空则抛出异常
     */
    protected void checkDataSourceListEmpty() throws DataSourceInvalidException {
        if (CollectionUtils.isEmpty(mysqlDataSourceList)) {
            throw new DataSourceInvalidException("mysqlDataSourceList is empty");
        }
    }

    /**
     * 从备选数据源列表中选中可用的数据源源，具体的选择会被委托到{@link DatasourceChoosePolicy DatasourceChoosePolicy}
     * 中， 选中的数据源需要使用{@link DatasourceChooseCallbackHandler
     * DatasourceChooseCallbackHandler}来回调做一些初始化操作。
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    protected void chooseMysqlDataSource() throws IOException, NoSuchAlgorithmException {
        T bak = currentDataSource;
        safeCloseCurrent();
        currentDataSource = recconectCurrent(bak);

        if (currentDataSource != null) {
            return;
        }

        // 使用匿名类，保证能够使用当前类中的一些基础方法
        currentDataSource =
                datasourceChoosePolicy.choose(mysqlDataSourceList, new DatasourceChooseCallbackHandler<T>() {

                    @Override
                    public void doCallback(T choosedDattasource) throws IOException, NoSuchAlgorithmException,
                            TimeoutException {
                        if (!choosedDattasource.isOpen()) {
                            StringBuffer sb =
                                    new StringBuffer("Choose new dataSource, [ip, port] is [")
                                            .append(choosedDattasource.getIpAddress()).append(", ")
                                            .append(choosedDattasource.getPort()).append("]");
                            getLogger().info(sb.toString());
                            prepareChoosedDatasouce(choosedDattasource);
                        }
                    }

                    @Override
                    public void logError(Exception e) {
                        getLogger().error(null, e);
                    }

                });
    }

    /**
     * 重新连接一下当前的数据源。
     * <p>
     * 当从socket读取数据失败或者超时时，在切换数据源之前先重新连接一下当前的数据，如果不成功 再试图切换数据源，这是因为数据读取失败往往不是挂了，而是网络抖动。
     * </p>
     *
     * @param ds 需要重连的数据源
     *
     * @return 重连成功的数据源，null表示重连失败或者当前没有数据源
     */
    private T recconectCurrent(T ds) {
        if (ds == null) {
            return ds;
        }

        getLogger().info("Reconnect current datasource {}:{}", ds.getIpAddress(), ds.getPort());

        try {
            prepareChoosedDatasouce(ds);
            return ds;
        } catch (NoSuchAlgorithmException e) {
            getLogger().error(null, e);
        } catch (IOException e) {
            getLogger().error(null, e);

        } catch (TimeoutException e) {
            getLogger().error(null, e);
        }
        return null;
    }

    /**
     * 安全的关闭数据源，并记录日志
     */
    protected void safeCloseCurrent() {
        // 关闭当前数据源
        if (currentDataSource != null) {
            StringBuffer sb =
                    new StringBuffer("DataSource is invalid and try to close, [ip, port] is [")
                            .append(currentDataSource.getIpAddress()).append(", ").append(currentDataSource.getPort())
                            .append("]");
            getLogger().warn(sb.toString());

            currentDataSource.close();
            currentDataSource = null;
        } else {
            getLogger().info("CurrentMysqlDataSource is null, pass closing");
        }
    }
}
