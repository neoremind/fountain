package net.neoremind.fountain.test.support;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.fountain.changedata.ChangeDataSet;

/**
 * 异步验证处理帮助类，异步的执行sql主动触发变化，异步的捕获事件变化并且针对每个变化进行验证
 *
 * @author zhangxu
 */
public abstract class AsyncChangeHandler implements ChangeHandler<ChangeDataSet> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncChangeHandler.class);

    /**
     * 执行sql
     */
    public abstract void doExecuteSql();

    /**
     * 验证每一行
     *
     * @param t        ChangeDataSet
     * @param eventSeq 变化的顺序，从0开始
     */
    public abstract void doCheckEachEvent(ChangeDataSet t, int eventSeq);

    @Override
    public void executeSql(final ExecuteCondition executeCondition) {
        AsyncThread.run(new Task() {
            @Override
            public void execute() throws InterruptedException {
                while (true) {
                    if (executeCondition.isReady()) {
                        break;
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info("Start to execute sql...");
                doExecuteSql();
                logger.info("End to execute sql..");
            }
        });
    }

    @Override
    public void checkEvent(final EventHolder eventHolder, final CountDownLatch latch,
                           final ExecuteCondition executeCondition) {
        AsyncThread.run(new Task() {
            @Override
            public void execute() throws InterruptedException {
                while (true) {
                    if (executeCondition.isReady()) {
                        break;
                    }
                }

                int eventSeq = 0;
                while (true) {
                    if (executeCondition.isStop()) {
                        break;
                    }
                    ChangeDataSet changeDataSet = eventHolder.poll();
                    if (changeDataSet != null) {
                        if (eventSeq < executeCondition.neglectChangeDataSetNum()) {
                            eventSeq++;
                            continue;
                        }
                        logger.info("=============== Check gtid={} start =================", changeDataSet.getGtId());
                        logger.info("ChangeDataSet: {}", changeDataSet);
                        doCheckEachEvent(changeDataSet, eventSeq);
                        logger.info("=============== Check gtid={} done =================", changeDataSet.getGtId());
                        latch.countDown();
                        eventSeq++;
                    }
                }
            }
        });
    }
}
