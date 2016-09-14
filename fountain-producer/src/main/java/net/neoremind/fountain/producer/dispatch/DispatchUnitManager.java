package net.neoremind.fountain.producer.dispatch;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.producer.dispatch.transcontrol.TransactionPolicy;

/**
 * 数据发送单元控制器。 一般来说以事务为粒度，或者以事务内某些表的数据或者这些数据的一部分为粒度。
 * 某些情况下可以为了提高下发的处理效率，可以将多个事务的数据打包下发，打包下发暂时不支持，后续可以增加。
 *
 * @author hexiufeng
 */
public interface DispatchUnitManager {
    /**
     * 接收一个BinlogEvent的对象并生成一个
     * {@link ChangeDataSet ChangeDataSet}对象，
     * 这个对象将被转化、打包、下发
     *
     * @param event        BaseLogEvent
     * @param instanceName instanceName
     *
     * @return ChangeDataSet
     */
    ChangeDataSet accept(final BaseLogEvent event, final String instanceName);

    /**
     * 设置事务控制策略
     *
     * @param transactionPolicy
     */
    void setTransactionPolicy(TransactionPolicy transactionPolicy);

    /**
     * 清除缓存中的event 数据。适用于如下情况：
     * <ul>
     * <li>分发数据以事务为单位,并且事务数据未收集完时，socket发生错误，切换数据源</li>
     * <li>事务已经完成，可以分发数据时</li>
     * </ul>
     */
    void cleanCachedEventData();
}
