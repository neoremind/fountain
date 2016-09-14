package net.neoremind.fountain.consumer.spi;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * 消费者的Actor，消费fountain产出的{@link ChangeDataSet}这个消息。
 * <p/>
 * 消费的形式可以很丰富:
 * <ul>
 * <li>写入本地文件</li>
 * <li>发送至MQ中间件</li>
 * <li>写入分布式存储HDFS</li>
 * <li>....</li>
 * </ul>
 * <p/>
 * 借鉴了<a href="https://en.wikipedia.org/wiki/Actor_model">Actor</a>模型，Actor作为一个独立的实体，
 * 可做消息传递通信中的一个环节，它没有状态，不改变消息本身，只做消费处理，或者调用别的actor继续完成任务。
 *
 * @author zhangxu
 */
public interface ConsumeActor {

    /**
     * 接收到数据库变化消息后的处理逻辑。
     *
     * @param event 数据库变化消息
     */
    void onReceive(ChangeDataSet event);

    /**
     * 当{@link #onReceive(ChangeDataSet)}处理完毕，并且安全退出/未抛出任何异常后，消费者会执行该方法。
     *
     * @param event  数据库变化消息
     * @param bridge 同步点桥接
     *
     * @see DisposeEventPositionBridge
     */
    void onSuccess(ChangeDataSet event, DisposeEventPositionBridge bridge);

    /**
     * 当{@link #onReceive(ChangeDataSet)}处理过程中，发生未不容的任何异常时，消费者回调该方法。
     *
     * @param event 数据库变化消息
     * @param e     未捕获的异常
     */
    void onUncaughtException(ChangeDataSet event, Exception e);

}
