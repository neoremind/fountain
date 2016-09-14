package net.neoremind.fountain.test.consumer;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;
import net.neoremind.fountain.test.support.EventHolder;

/**
 * 测试用JVM内消费者，是fountain框架的{@link Consumer}实现
 *
 * @author zhangxu
 */
public class TestConsumer implements Consumer {

    /**
     * 事件收集器
     */
    private EventHolder eventHolder;

    /**
     * 消费事件，把{@link ChangeDataSet}放入{@link EventHolder}中
     *
     * @param event
     * @param <T>
     *
     * @return
     */
    @Override
    public <T> boolean consume(T event) {
        ChangeDataSet ds = (ChangeDataSet) event;
        eventHolder.put(ds);
        return true;
    }

    private void savePoint(ChangeDataSet ds) {
        if (bridge != null) {
            bridge.getDisposeEventPosition(ds.getInstanceName()).saveSyncPoint(ds.getSyncPoint());
        }
    }

    private DisposeEventPositionBridge bridge;

    public DisposeEventPositionBridge getBridge() {
        return bridge;
    }

    public void setBridge(DisposeEventPositionBridge bridge) {
        this.bridge = bridge;
    }

    public EventHolder getEventHolder() {
        return eventHolder;
    }

    public void setEventHolder(EventHolder eventHolder) {
        this.eventHolder = eventHolder;
    }
}
