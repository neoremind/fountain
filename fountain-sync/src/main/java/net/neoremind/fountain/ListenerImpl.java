package net.neoremind.fountain;

import net.neoremind.fountain.common.mq.FountainMQ;
import net.neoremind.fountain.common.mq.MultiPermitsMemFountainMQ;
import net.neoremind.fountain.consumer.spi.def.DefaultConsumerWorkflow;
import net.neoremind.fountain.consumer.support.fountainmq.FountainMQMessageListener;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * 使用队列做消息获取的监听器实现
 *
 * @author zhangxu
 */
public class ListenerImpl implements Listener<FountainMQ> {

    FountainMQMessageListener listener;

    FountainMQ mq;

    public ListenerImpl(BinlogSyncBuilderTemplate builder, DisposeEventPositionBridge bridge) {
        mq = new MultiPermitsMemFountainMQ(
                Either.or(Defaults.MESSAGE_QUEUE_SIZE).fromNullable(builder.getMessageQueueSize()));
        listener = newMessageListener(mq, builder.getConsumer(), bridge);
    }

    private FountainMQMessageListener newMessageListener(FountainMQ mq, EventConsumer consumer,
                                                         DisposeEventPositionBridge bridge) {
        if (consumer == null) {
            consumer = Defaults.CONSUMER;
        }
        FountainMQMessageListener listener = new FountainMQMessageListener();
        listener.setFmq(mq);
        consumer.setPositionBridge(bridge);
        DefaultConsumerWorkflow consumerWorkflow = new DefaultConsumerWorkflow();
        consumerWorkflow.setConsumer(consumer);
        listener.setWorkflow(consumerWorkflow);
        return listener;
    }

    @Override
    public void start() {
        listener.start();
    }

    @Override
    public void destroy() {
        listener.destroy();
        mq = null;
    }

    @Override
    public FountainMQ getQueue() {
        return mq;
    }
}
