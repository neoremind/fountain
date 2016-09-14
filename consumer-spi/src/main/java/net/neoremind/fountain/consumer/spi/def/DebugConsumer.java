package net.neoremind.fountain.consumer.spi.def;

import net.neoremind.fountain.consumer.spi.ConsumerWorkflow;

/**
 * 调试模式下的消费流程，不做任何事情
 *
 * @author hexiufeng
 */
public class DebugConsumer implements ConsumerWorkflow {

    @Override
    public boolean doConsume(Object message) {
        // do nothing
        return true;
    }

}
