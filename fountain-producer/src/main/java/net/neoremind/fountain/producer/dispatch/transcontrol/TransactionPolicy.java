package net.neoremind.fountain.producer.dispatch.transcontrol;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.producer.dispatch.TableDataProvider;

/**
 * 事务控制策略，在某些情况下需要控制事务中下发数据的数据。
 * <p>
 * 比如：需要整事务下发处理时有些事务会比较大，这个大事务中的某些数据很多但是无用的， 需要丢弃这部分数据.
 * </p>
 *
 * @author hexiufeng
 */
public interface TransactionPolicy {
    /**
     * 处理指定事件的回调,支持如下方法：<br>
     * <ul>
     * <li>指定事件是否为rollback事件，用于binlog协议</li>
     * <li>接收事件数据</li>
     * </ul>
     *
     * @author hexiufeng
     */
    public interface EventCallback {
        /**
         * 指定事件是否为rollback事件，用于binlog协议
         *
         * @param event 事件
         *
         * @return true or false
         */
        boolean isRollback(final BaseLogEvent event);

        /**
         * 事件是否是一个row data事件
         *
         * @param event 事件
         *
         * @return true or false
         */
        boolean isRowDataEvent(final BaseLogEvent event);

        /**
         * 根据事件获取表数据提供者
         *
         * @param event BaseLogEvent
         *
         * @return TableDataProvider
         */
        TableDataProvider getTableDataProvider(BaseLogEvent event);

        /**
         * 接收Row data事件的数据并返回当前的ChangeDataSet对象，正常情况下返回的ChangeDataSet
         * 与参数ds时相同的，但也有例外，在NonTransactionPolicy实现中，可能返回null
         *
         * @param event 指定事件
         * @param ds ChangeDataSet
         * @return ChangeDataSet
         */
        // ChangeDataSet addRowDataEvent(final BaseLogEvent event, final
        // ChangeDataSet ds);

        /**
         * 执行完accept操作后的回调
         *
         * @param event
         */
        void afterAccept(BaseLogEvent event);
    }

    /**
     * 获取存在的dataset
     *
     * @return ChangeDataSet
     */
    ChangeDataSet getValidOutputDataSet();

    /**
     * 接收当前事件
     *
     * @param instanceName 生成者实例名称
     * @param event        事件
     * @param callback     处理回调
     */
    void acceptEvent(String instanceName, BaseLogEvent event,
                     EventCallback callback);

    //    /**
    //     * 获取需要丢弃数据的表，这些表在事务内可能数据量大，但没有业务价值
    //     *
    //     * @param entries entries
    //     * @return String[]
    //     */
    //    String[] getDiscardTable(final Collection<TableSizeEntry> entries);

    /**
     * 是否在事务中，需要事务控制
     *
     * @return boolean
     */
    boolean isInTrans();

    /**
     * 清零,对象可重用
     */
    void clear();
}
