package net.neoremind.fountain;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * @author zhangxu
 */
public abstract class EventConsumer implements Consumer {

    private DisposeEventPositionBridge positionBridge;

    @Override
    public <T> boolean consume(T event) {
        try {
            onEvent((ChangeDataSet) event);
        } catch (Throwable t) {
            onFail((ChangeDataSet) event, t);
            return false;
        }
        onSuccess((ChangeDataSet) event, positionBridge);
        return true;
    }

    public abstract void onEvent(ChangeDataSet changeDataSet);

    public abstract void onSuccess(ChangeDataSet changeDataSet, DisposeEventPositionBridge positionBridge);

    public abstract void onFail(ChangeDataSet changeDataSet, Throwable t);

    public void setPositionBridge(DisposeEventPositionBridge positionBridge) {
        this.positionBridge = positionBridge;
    }
}
