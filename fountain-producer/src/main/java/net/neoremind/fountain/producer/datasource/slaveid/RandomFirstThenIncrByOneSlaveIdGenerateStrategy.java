package net.neoremind.fountain.producer.datasource.slaveid;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 随机的slaveId生成器，避免多个实例启动的时候MySQL只能接受一个做同步，其他的直接拒绝了。
 * <p/>
 * 第一次随机生成后，后面的都默认递增1
 *
 * @author zhangxu
 */
public class RandomFirstThenIncrByOneSlaveIdGenerateStrategy implements SlaveIdGenerateStrategy<Integer> {

    /**
     * 随机起始值
     */
    private int start;

    /**
     * 随机终止值
     */
    private int end;

    /**
     * 当前slaveId
     */
    private AtomicInteger currSlaveId = new AtomicInteger(0);

    private final Random random = new Random();

    public static int next(Random random, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end must not smaller than begin");
        } else {
            int minus = random.nextInt(end - start + 1);
            return start + minus;
        }
    }

    @Override
    public Integer get() {
        if (currSlaveId.get() == 0) {
            currSlaveId = new AtomicInteger(next(random, start, end));
            return currSlaveId.get();
        }
        return currSlaveId.incrementAndGet();
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
