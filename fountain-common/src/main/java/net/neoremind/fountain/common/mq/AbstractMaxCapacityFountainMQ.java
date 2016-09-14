package net.neoremind.fountain.common.mq;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.BinlogTraceable;

/**
 * 内存队列实现的抽象类.
 * 使用无界BlockingQueue保存消息，使用guardLimit控制消息数量
 *
 * @author hexiufeng,zhangxu
 */
public abstract class AbstractMaxCapacityFountainMQ implements FountainMQ {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMaxCapacityFountainMQ.class);

    private final BlockingQueue<BinlogTraceable> queue = new LinkedBlockingQueue<BinlogTraceable>();
    /**
     * 控制queue消息数量的信号量
     */
    private final Semaphore guardLimit;
    /**
     * 队列容量上限
     */
    protected final int limitSize;

    /**
     * 构造方法
     *
     * @param limit 队列容量的上限
     */
    protected AbstractMaxCapacityFountainMQ(int limit) {
        guardLimit = new Semaphore(limit);
        limitSize = limit;
    }

    /**
     * 从一个数据消息中获取需要权限的个数
     *
     * @param e 消息
     *
     * @return 权限个数
     */
    protected abstract int getRequiredPermits(BinlogTraceable e);

    @Override
    public void push(BinlogTraceable e) {
        while (true) {
            try {
                int waitTimes = 0;
                int permit = getRequiredPermits(e);
                while (!guardLimit.tryAcquire(permit, 500, TimeUnit.MILLISECONDS)) {
                    if (waitTimes == 0) {
                        LOGGER.warn("wait 500ms for {}", permit);
                    }
                    waitTimes++;
                }
                if (waitTimes > 0) {
                    LOGGER.warn("wait times {} for {}", waitTimes, permit);
                }
                queue.put(e);
                break;
            } catch (InterruptedException e1) {
                // ignore
                continue;
            }
        }
    }

    @Override
    public boolean push(BinlogTraceable e, long timeout) {
        long start = System.currentTimeMillis();
        boolean ok = false;
        long used = 0;
        while (true) {
            try {
                int permits = getRequiredPermits(e);
                ok = guardLimit.tryAcquire(permits, timeout - used, TimeUnit.MILLISECONDS);
                if (ok) {
                    queue.put(e);
                }
                return ok;
            } catch (InterruptedException e1) {
                // ignore
                used = System.currentTimeMillis() - start;
                if (used > 0) {
                    continue;
                }
                return ok;
            }
        }
    }

    @Override
    public BinlogTraceable pop() {
        BinlogTraceable e = null;
        while (true) {
            try {
                e = queue.take();
                if (e != null) {
                    guardLimit.release(getRequiredPermits(e));
                }
                return e;
            } catch (InterruptedException e1) {
                // ignore
                continue;
            }
        }
    }

    @Override
    public BinlogTraceable pop(long timeout) {
        long start = System.currentTimeMillis();
        BinlogTraceable e = null;
        long used = 0;
        while (true) {
            try {
                e = queue.poll(timeout - used, TimeUnit.MILLISECONDS);
                if (e != null) {
                    guardLimit.release(getRequiredPermits(e));
                }
                return e;
            } catch (InterruptedException e1) {
                // ignore
                used = System.currentTimeMillis() - start;
                if (used > 0) {
                    continue;
                }
                return e;
            }
        }
    }

}
