package net.neoremind.fountain.support;

import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * ThreadLocal线程保存辅助类
 *
 * @author zhangxu
 */
public class ThreadHolder {

    /**
     * SyncPoint的内存缓存，缓存当producer中已经接收到的最后的SyncPoint，当数据源不可用并选择新的数据源时 直接使用该SyncPoint继续接收。
     * <p>
     * 当fountain-producer和fountain-consumer部署在同一进程中时，生产者接收的数据会放入一个内存mq，
     * 消费者从该内存mq获取消息消费，消费完成后消费者记录当前已经消费的SyncPoint，此时生产者的SyncPoint远远大于 已经消费的SyncPoint，
     * 如果重新选择数据源时从消费者记录的gtid恢复数据接收，则会出现大量重复数据,使用 本方法将避免如此问题。
     * </p>
     */
    public static final ThreadLocal<SyncPoint> SYNC_POINT_CACHE = new ThreadLocal<SyncPoint>();

    /**
     * 事务上下文
     */
    public static final ThreadLocal<TrxContext> TRX_CONTEXT = new ThreadLocal<TrxContext>();

    /**
     * 清空线程上下文
     */
    public static final void cleanTrxContext() {
        TRX_CONTEXT.set(null);
    }

    /**
     * 初始化线程上下文
     */
    public static final void setTrxContext(TrxContext trxContext) {
        cleanTrxContext();
        TRX_CONTEXT.set(trxContext);
    }

    /**
     * 获取事务上下文
     *
     * @return TrxContext
     */
    public static TrxContext getTrxContext() {
        return TRX_CONTEXT.get();
    }
}
