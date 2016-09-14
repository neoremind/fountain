package net.neoremind.fountain.support;

import java.util.Map;

import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.util.CollectionUtil;

/**
 * binlog event的回调监听器
 *
 * @author zhangxu
 */
public class EventListener {

    /**
     * 类型到处理回调的map
     */
    private Map<Class<? extends BaseLogEvent>, EventCallback> eventCallbackMap = CollectionUtil.createHashMap(5);

    /**
     * 默认构造方法
     */
    private EventListener() {

    }

    /**
     * 静态构造方法
     *
     * @return EventListener
     */
    public static EventListener factory() {
        return new EventListener();
    }

    /**
     * 为某种binlog event添加回调
     *
     * @param clazz event类型
     * @param cb    事件回调
     */
    public void addCallback(Class<? extends BaseLogEvent> clazz, EventCallback cb) {
        eventCallbackMap.put(clazz, cb);
    }

    /**
     * 当fountain接收到某个binlog event时候会根据具体类型，找到回调器，然后调用，一般会设置一些变量信息到{@link TrxContext}中
     *
     * @param event      binlog事件
     * @param trxContext 事务上下文
     */
    public void handle(BaseLogEvent event, TrxContext trxContext) {
        EventCallback cb = eventCallbackMap.get(event.getClass());
        if (cb != null) {
            cb.handle(event, trxContext);
        }
    }

}
