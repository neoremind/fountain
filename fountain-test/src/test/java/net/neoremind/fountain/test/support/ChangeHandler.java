package net.neoremind.fountain.test.support;

import java.util.concurrent.CountDownLatch;

import net.neoremind.fountain.test.consumer.TestConsumer;

/**
 * 变化的处理接口
 *
 * @author zhangxu
 */
public interface ChangeHandler<T> {

    /**
     * 根据执行条件选择是否执行sql，使用{@link ExecuteCondition#isReady()}
     *
     * @param executeCondition
     */
    void executeSql(ExecuteCondition executeCondition);

    /**
     * 预期的发生变化的数量
     *
     * @return
     */
    int getTransactionNumber();

    /**
     * 验证变化事件
     *
     * @param eventHolder      事件接收器，{@link TestConsumer}会发布事件到这个阻塞队列中
     * @param latch            每发生一个变化就得count down一下闭锁
     * @param executeCondition 退出验证的条件，使用{@link ExecuteCondition#isStop()}
     */
    void checkEvent(EventHolder eventHolder, CountDownLatch latch, ExecuteCondition executeCondition);

}
