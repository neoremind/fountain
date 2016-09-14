package net.neoremind.fountain.runner;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.consumer.spi.ConsumeActor;
import net.neoremind.fountain.consumer.spi.ConsumeActorAware;
import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.consumer.spi.def.SimpleConsumeActor;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * fountain-runner使用的默认消费者，使用模板方法模式，将消费逻辑委托给{@link ConsumeActor}去实现
 *
 * @author zhangxu
 */
@PluggableConsumeActorEnabled
public class DefaultConsumer implements Consumer, ConsumeActorAware {

    /**
     * 消费者Actor，默认使用{@link SimpleConsumeActor}
     */
    private ConsumeActor consumeActor = new SimpleConsumeActor();

    /**
     * 同步点桥接
     */
    private DisposeEventPositionBridge bridge;

    @Override
    public <T> boolean consume(T event) {
        try {
            consumeActor.onReceive((ChangeDataSet) event);
        } catch (Exception e) {
            consumeActor.onUncaughtException((ChangeDataSet) event, e);
        }
        consumeActor.onSuccess((ChangeDataSet) event, bridge);
        return true;
    }

    @Override
    public void setConsumeActor(ConsumeActor consumeActor) {
        this.consumeActor = consumeActor;
    }

    public void setBridge(DisposeEventPositionBridge bridge) {
        this.bridge = bridge;
    }

}
