package net.neoremind.fountain.common.mq;

import net.neoremind.fountain.changedata.BinlogTraceable;

/**
 * 每个数据消息占用多个队列容量的队列。如果队列剩余容量不够，则一直等到或者超时
 * 当多个producer数据发往同一个mq时不要使用本实现，因为其他producer可能一直发送小事务，mq1内容容量
 * 一直不能被释放出来，那么一个producer的大事务可能永远不能被处理
 *
 * @author hexiufeng
 */
public class MultiPermitsMemFountainMQ extends AbstractMaxCapacityFountainMQ implements FountainMQ {

    /**
     * 构造方法
     *
     * @param limit 容量上限
     */
    public MultiPermitsMemFountainMQ(int limit) {
        super(limit);
    }

    @Override
    public boolean isExceedMaxCapacity(int len) {
        return len > limitSize;
    }

    @Override
    protected int getRequiredPermits(BinlogTraceable e) {
        return e.getDataSize();
    }

}
