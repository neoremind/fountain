package net.neoremind.fountain.common.mq;

import net.neoremind.fountain.changedata.BinlogTraceable;

/**
 * {@link FountainMQ FountainMQ}缺省实现，每个放入队列的对象
 * 占用队列容量数为1
 * <p/>
 * 封装BlockingQueue,必须有界，这是为了防止内存oom
 *
 * @author hexiufeng
 */
public class MemFountainMQ extends AbstractMaxCapacityFountainMQ implements FountainMQ {

    public MemFountainMQ(int limit) {
        super(limit);
    }

    @Override
    public boolean isExceedMaxCapacity(int len) {
        return false;
    }

    @Override
    protected int getRequiredPermits(BinlogTraceable e) {
        return 1;
    }

}
