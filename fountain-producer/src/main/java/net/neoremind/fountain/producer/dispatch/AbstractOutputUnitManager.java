package net.neoremind.fountain.producer.dispatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy;
import net.neoremind.fountain.producer.dispatch.transcontrol.TransactionPolicy;
import net.neoremind.fountain.rowbaselog.event.QueryLogEvent;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;

/**
 * 数据下发单元控制的抽象实现
 *
 * @author hexiufeng, zhangxu
 */
@UnThreadSafe
public abstract class AbstractOutputUnitManager implements DispatchUnitManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOutputUnitManager.class);

    /**
     * 事务控制策略，缺省是非完整事务控制策略
     */
    private TransactionPolicy transactionPolicy = new NonTransactionPolicy();

    @Override
    public ChangeDataSet accept(final BaseLogEvent event,
                                final String instanceName) {
        transactionPolicy.acceptEvent(instanceName, event, new TransactionPolicy.EventCallback() {

            @Override
            public boolean isRollback(final BaseLogEvent event) {
                return isRollbackEvent(event);
            }

            @Override
            public boolean isRowDataEvent(final BaseLogEvent event) {
                return isValidRowDataEvent(event);
            }

            @Override
            public TableDataProvider getTableDataProvider(BaseLogEvent event) {
                return createTableDataProvider(event);
            }

            @Override
            public void afterAccept(BaseLogEvent event) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Accept {} for gtId={}, currState={} timestamp={}", event.getClass().getSimpleName(),
                            ThreadHolder.getTrxContext().getCurrGtId(),
                            ThreadHolder.getTrxContext(),
                            event.getEventHeader().getTimestamp());
                }

            }
        });
        if (finishDataSet(event, transactionPolicy.isInTrans())) {
            ChangeDataSet ds = transactionPolicy.getValidOutputDataSet();
            cleanCachedEventData();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Finish gtId={}", ThreadHolder.getTrxContext().getCurrGtId());
            }
            return ds;
        }

        return null;
    }

    @Override
    public void cleanCachedEventData() {
        transactionPolicy.clear();
    }

    /**
     * 判断当前事件是否是rollback事件
     *
     * @param event 当前事件
     *
     * @return true or false
     */
    private boolean isRollbackEvent(BaseLogEvent event) {
        if (event instanceof QueryLogEvent) {
            String query = ((QueryLogEvent) event).query;
            if (StringUtils.isEmpty(query)) {
                return false;
            }
            query = query.toLowerCase().trim();
            return query.equals("rollback");
        }
        return false;
    }

    /**
     * 判断event是否是一个含有row data的event
     *
     * @param event
     *
     * @return
     */
    protected abstract boolean isValidRowDataEvent(BaseLogEvent event);

    /**
     * 是否当前的数据可以被分发
     *
     * @param event   当前的事件
     * @param inTrans 是否需要整事务分发
     *
     * @return 是否可以被分发
     */
    protected abstract boolean finishDataSet(BaseLogEvent event, boolean inTrans);

    /**
     * 由BaseLogEvent组装成TableDataProvider
     *
     * @param event 事件
     *
     * @return TableDataProvider
     */
    protected abstract TableDataProvider createTableDataProvider(
            BaseLogEvent event);

    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public void setTransactionPolicy(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }

}
