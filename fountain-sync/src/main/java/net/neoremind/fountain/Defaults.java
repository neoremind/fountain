package net.neoremind.fountain;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;

/**
 * 默认值
 *
 * @author zhangxu
 */
class Defaults {

    /**
     * producer的名称
     */
    final static String DEFAULT_PRODUCER_NAME = "binglog-sync-producer";

    /**
     * producer和consumer之间的缓存队列长度
     */
    final static int MESSAGE_QUEUE_SIZE = 20000;

    /**
     * 默认的消费者实现，仅打印到控制台
     */
    final static EventConsumer CONSUMER = new EventConsumer() {

        @Override
        public void onEvent(ChangeDataSet changeDataSet) {
            System.out.println(changeDataSet);
        }

        @Override
        public void onSuccess(ChangeDataSet changeDataSet, DisposeEventPositionBridge positionBridge) {

        }

        @Override
        public void onFail(ChangeDataSet changeDataSet, Throwable t) {

        }
    };

}
