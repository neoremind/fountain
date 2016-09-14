package net.neoremind.fountain;

import java.util.List;

/**
 * 可迭代的Binlog同步器实现，即可以单分片（Single Shard），又可以多数据源（Multi Shards）
 *
 * @author zhangxu
 */
public class IterableBinlogSyncer extends ListIteration<BinlogSyncer> implements BinlogSyncer, Iteration<BinlogSyncer> {

    /**
     * 不管是单分片还是多分片，都汇聚到同一个{@link Listener}处理
     */
    private Listener<?> listener;

    /**
     * 构造方法
     *
     * @param syncerList 多分片的列表，每个分片内部可以做高可用配置多个地址IP:PORT，但是逻辑上他们属于一个数据源分片
     * @param listener   增量消息监听器
     */
    public IterableBinlogSyncer(List<BinlogSyncer> syncerList, Listener<?> listener) {
        list.addAll(syncerList);
        this.listener = listener;
    }

    /**
     * 构造方法
     *
     * @param syncer   单个分片，分片内部可以做高可用配置多个地址IP:PORT，但是逻辑上他们属于一个数据源分片
     * @param listener 增量消息监听器
     */
    public IterableBinlogSyncer(BinlogSyncer syncer, Listener<?> listener) {
        list.add(syncer);
        this.listener = listener;
    }

    @Override
    public void start() {
        for (; hasNext(); ) {
            BinlogSyncer syncer = next();
            if (syncer instanceof Callbackable) {
                ((Callbackable) syncer).callback(listener);
            }
            syncer.start();
        }
        listener.start();
    }

    @Override
    public void stop() {
        for (; hasNext(); ) {
            BinlogSyncer syncer = next();
            syncer.stop();
        }
        listener.destroy();
    }
}
