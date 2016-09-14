package net.neoremind.fountain.test.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;

/**
 * {@link ChangeDataSet}变化事件的收集器
 *
 * @author zhangxu
 */
public class EventHolder {

    private static final Logger logger = LoggerFactory.getLogger(EventHolder.class);

    /**
     * 阻塞队列
     */
    private final BlockingQueue<ChangeDataSet> queue = new LinkedBlockingQueue<ChangeDataSet>();

    /**
     * 调用{@link #put(ChangeDataSet)}的次数
     */
    private AtomicInteger eventCount = new AtomicInteger(0);

    /**
     * 事件收集器的回调，主要是打印日志和计数
     */
    private EventHolderCallback<ChangeDataSet> cb = new EventHolderCallback<ChangeDataSet>() {
        @Override
        public void onPut(ChangeDataSet t) {
            logger.info("Receive groupId=" + t.getGtId());
            eventCount.incrementAndGet();
        }
    };

    /**
     * 加入事件
     *
     * @param t
     */
    public void put(ChangeDataSet t) {
        try {
            cb.onPut(t);
            queue.put(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取出事件，当取到不到则阻塞，阻塞5000ms
     *
     * @return
     */
    public ChangeDataSet poll() {
        try {
            return queue.poll(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * builder模式设置回调并返回自己的引用
     *
     * @param cb
     *
     * @return
     */
    public EventHolder setCb(EventHolderCallback cb) {
        this.cb = cb;
        return this;
    }

    /**
     * 返回调用{@link #put(ChangeDataSet)}的次数
     *
     * @return
     */
    public int getEventCount() {
        return eventCount.get();
    }

}
