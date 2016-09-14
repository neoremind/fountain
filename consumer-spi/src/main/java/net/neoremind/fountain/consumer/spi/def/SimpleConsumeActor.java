package net.neoremind.fountain.consumer.spi.def;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.ConsumeActor;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * 默认的消费者Actor
 * <p/>
 * 最简单的操作，<br/>
 * 1）{@link #onReceive(ChangeDataSet)}方法打印变化的增量<br/>
 * 2）{@link #onSuccess(ChangeDataSet, DisposeEventPositionBridge)}通过同步点桥接记录<code>SyncPoint</code><br/>
 * 3）发生异常打印堆栈<br/>
 *
 * @author zhangxu
 */
public class SimpleConsumeActor implements ConsumeActor {

    @Override
    public void onReceive(ChangeDataSet event) {
        System.out.println(event);
    }

    @Override
    public void onSuccess(ChangeDataSet event, DisposeEventPositionBridge bridge) {
        if (bridge != null) {
            bridge.getDisposeEventPosition(event.getInstanceName()).saveSyncPoint(event.getSyncPoint());
        }
    }

    @Override
    public void onUncaughtException(ChangeDataSet event, Exception e) {
        e.printStackTrace();
    }

}
