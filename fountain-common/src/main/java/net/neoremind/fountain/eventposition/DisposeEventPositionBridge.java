package net.neoremind.fountain.eventposition;

/**
 * 用于fountain-producer和fountain-consumer部署在同一个jvm中的场景，此时需要消费者来记录
 * 当前已经成功处理的消息的gt id，使用该类来解耦生产者和消费者
 *
 * @author hexiufeng
 */
public interface DisposeEventPositionBridge {
    /**
     * 生产者把自己的名称和所使用的
     * {@link DisposeEventPosition DisposeEventPosition}注册进来
     *
     * @param producerName
     * @param disp
     */
    void register(String producerName, DisposeEventPosition disp);

    /**
     * 消费者根据producerName获取{@link DisposeEventPosition DisposeEventPosition}对象来记录gt id
     *
     * @param producerName
     *
     * @return
     */
    DisposeEventPosition getDisposeEventPosition(String producerName);
}
