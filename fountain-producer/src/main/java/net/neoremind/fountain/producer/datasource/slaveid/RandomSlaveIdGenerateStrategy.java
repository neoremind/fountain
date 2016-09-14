package net.neoremind.fountain.producer.datasource.slaveid;

import java.util.Random;

/**
 * 随机的slaveId生成器，避免多个实例启动的时候MySQL只能接受一个做同步，其他的直接拒绝了。
 *
 * @author zhangxu
 */
public class RandomSlaveIdGenerateStrategy implements SlaveIdGenerateStrategy<Integer> {

    /**
     * 随机起始值
     */
    private int start;

    /**
     * 随机终止值
     */
    private int end;

    /**
     * 默认初始后不再变更slaveId，如果想每次都变更请使用{@link ResetEveryTime}
     */
    private Resettable resettable = new NoReset();

    /**
     * 当前slaveId
     */
    private Integer currSlaveId;

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
        if (resettable.isEnableReset()) {
            currSlaveId = next(random, start, end);
        }
        return currSlaveId;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
