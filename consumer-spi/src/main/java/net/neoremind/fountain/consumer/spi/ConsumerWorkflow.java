package net.neoremind.fountain.consumer.spi;

/**
 * 消费一个数据变化的工作流程，一般包括解包、和消费
 *
 * @author zhangxu
 */
public interface ConsumerWorkflow {
    /**
     * 消费一个数据变化, 返回true表示消费成功,可以继续消费下一个， 如果返回false，上层调用方可能回滚事务，需要重复消费
     *
     * @param message 消息
     *
     * @return 是否消费成功
     */
    boolean doConsume(Object message);
}
