package net.neoremind.fountain.consumer.spi.def;

import org.slf4j.Logger;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * 直接消费ChangeDataSet对象的消费者抽象实现。
 * <p>
 * 直接消费ChangeDataSet对象的情况一般适用于producer-consumer部署在同一进程内的情况， 此时通过内存mq进行传输，ChangeDataSet不需要被打包和解包
 * </p>
 *
 * @author hexiufeng
 */
public abstract class AbstractConsumeSingleChangeSetConsumer implements Consumer {

    private DisposeEventPositionBridge disposeEventPositionBridge;

    @Override
    public <T> boolean consume(T event) {
        if (event instanceof ChangeDataSet) {
            ChangeDataSet ds = (ChangeDataSet) event;
            outputCore(ds);
            if (ds.getSyncPoint() != null) {
                recordSyncPoint(ds);
            }
            return true;
        }
        throw new RuntimeException("event is not ChangeDataSet.");

    }

    /**
     * 记录同步点
     *
     * @param event 事件
     */
    private void recordSyncPoint(ChangeDataSet ds) {
        if (disposeEventPositionBridge == null) {
            return;
        }
        try {
            disposeEventPositionBridge.getDisposeEventPosition(ds.getInstanceName()).saveSyncPoint(ds.getSyncPoint());
        } catch (RuntimeException e) {
            getLogger().error("record sync point failed.", e);
        }
    }

    /**
     * 输出event
     *
     * @param event ChangeDataSet
     *
     * @throws RuntimeException RuntimeException
     */
    protected abstract void outputCore(ChangeDataSet event) throws RuntimeException;

    /**
     * 获取当前日志
     *
     * @return Logger
     */
    protected abstract Logger getLogger();

    public void setDisposeEventPositionBridge(DisposeEventPositionBridge disposeEventPositionBridge) {
        this.disposeEventPositionBridge = disposeEventPositionBridge;
    }
}
