package net.neoremind.fountain.eventposition;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以固定的时间频率异步的存储同步点
 *
 * @author zhangxu
 */
public class AsyncFixedRateDisposeEventPosition extends AbstractProxyDisposeEventPosition
        implements DisposeEventPosition {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFixedRateDisposeEventPosition.class);

    /**
     * 定时写point的间隔周期
     */
    private long periodMs = 20000L;

    /**
     * 第一次写point的时间
     */
    private long initDelayMs = 6000L;

    private ScheduledExecutorService scheduler;

    private final SyncPointContext context = new SyncPointContext();

    private DisposeEventPositionBridge disposeEventPositionBridge;

    /**
     * 定时保存event point的上下文
     */
    private class SyncPointContext {
        /**
         * 已经保存的同步点，只在scheduler线程内使用，没有线程安全问题
         */
        private SyncPoint prev;

        /**
         * 需要保存的同步点，两个会同时访问
         */
        private SyncPoint curr;
    }

    /**
     * 初始化
     */
    public void init() {
        LOGGER.info("Start {} in background, initDelayMs={}, periodMs={}", this.getClass().getSimpleName(),
                initDelayMs, periodMs);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                saveSyncPointByDelegate();
            }
        }, initDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public void saveSyncPoint(SyncPoint point) {
        context.curr = point;
    }

    /**
     * 这里简化下从prev中获取
     *
     * @return SyncPoint
     */
    @Override
    // FIXME
    public SyncPoint loadSyncPoint() {
        if (context.prev != null) {
            return context.prev;
        }
        return super.loadSyncPoint();
    }

    private void saveSyncPointByDelegate() {
        if (context.curr != null) {
            if (context.prev == null ||
                    !ByteBuffer.wrap(context.prev.toBytes()).equals(ByteBuffer.wrap(context.curr.toBytes()))) {
                delegate.saveSyncPoint(context.curr);
            }
            context.prev = context.curr;
        }
    }

    @Override
    public void registerInstance(String insName) {
        super.registerInstance(insName);
        if (disposeEventPositionBridge != null) {
            disposeEventPositionBridge.register(insName, this);
        }
    }

    public void setDisposeEventPositionBridge(DisposeEventPositionBridge disposeEventPositionBridge) {
        this.disposeEventPositionBridge = disposeEventPositionBridge;
    }

    public long getInitDelayMs() {
        return initDelayMs;
    }

    public void setInitDelayMs(long initDelayMs) {
        this.initDelayMs = initDelayMs;
    }

    public long getPeriodMs() {
        return periodMs;
    }

    public void setPeriodMs(long periodMs) {
        this.periodMs = periodMs;
    }

}
