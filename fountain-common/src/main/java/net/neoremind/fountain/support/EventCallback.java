package net.neoremind.fountain.support;

import net.neoremind.fountain.event.BaseLogEvent;

/**
 * {@link BaseLogEvent}处理的回调接口
 *
 * @author zhangxu
 * @see BaseLogEvent
 * @see TrxContext
 */
public interface EventCallback<T extends BaseLogEvent> {

    /**
     * 在当前ThreadLocal上下文中处理某个event，一般的操作是根据event传输数据的内容设置context一些上下文
     *
     * @param event      binglog事件
     * @param trxContext 事务上下文
     */
    void handle(T event, TrxContext trxContext);

}
