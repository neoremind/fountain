package net.neoremind.fountain.common.mq;

import net.neoremind.fountain.changedata.BinlogTraceable;

/**
 * fountain需要的内存mq的抽象描述。用于fountain-producer和consumer
 * 部署在同一个jvm进程的场景
 *
 * @author hexiufeng,zhangxu
 */
public interface FountainMQ {
    /**
     * push一个消息，对于有界mq，如果容量已满，此方法会堵塞直到容量被至少是否一个unit
     *
     * @param e 需要加入队列的元素
     */
    void push(BinlogTraceable e);

    /**
     * push 一个消息，支持超时的版本，如果容量已满，此方法会堵塞直到容量被至少是否一个unit
     * 或者超时，超时时返回false
     *
     * @param e       需要加入队列的元素
     * @param timeout 单位ms
     *
     * @return 是否成功
     */
    boolean push(BinlogTraceable e, long timeout);

    /**
     * 从队列中读取一个消息，如果队列中没有消息，则堵塞直到有一个消息进入
     *
     * @return 消息数据
     */
    BinlogTraceable pop();

    /**
     * 从队列中读取一个消息,支持超时版本，如果队列中没有消息，则堵塞直到有一个消息进入
     * 或者超时，超时时返回false
     *
     * @param timeout 单位ms
     *
     * @return 消息数据
     */
    BinlogTraceable pop(long timeout);

    /**
     * 是否超过队列的最大容量
     *
     * @param len 长度
     *
     * @return 是否超出
     */
    boolean isExceedMaxCapacity(int len);
}
