package net.neoremind.fountain.consumer.spi.def;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.ConsumeActor;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * 默认的消费者Actor，什么都不做，继承的子类可覆盖方法，不用全部重写。
 *
 * @author zhangxu
 */
public class DefaultConsumeActor implements ConsumeActor {

    @Override
    public void onReceive(ChangeDataSet event) {
        // do nothing
    }

    @Override
    public void onSuccess(ChangeDataSet event, DisposeEventPositionBridge bridge) {
        // do nothing
    }

    @Override
    public void onUncaughtException(ChangeDataSet event, Exception e) {
        // do nothing
    }

}
