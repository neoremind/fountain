package net.neoremind.fountain;

import net.neoremind.fountain.eventposition.DisposeEventPosition;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridgeImpl;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategy;
import net.neoremind.fountain.producer.datasource.BinlogDataSource;

/**
 * binlog同步器builder
 *
 * @author zhangxu
 */
public class BinlogSyncBuilder extends BinlogSyncBuilderTemplate implements Builder<BinlogSyncer> {

    /**
     * 接收到消息的生产者名称
     */
    private String producerName;

    /**
     * 数据源的一种基于字符串的配置
     */
    private DataSource dataSource;

    /**
     * dump binlog的策略。
     * <p/>
     * 当从库连接到主库以后，从库向主库发送一条dump命令，开始复制过程。<br/>
     * binlog dump策略接口，子类泛化成为具体的实施办法，主要用于屏蔽{@link BinlogDataSource}向MySQL
     * server发送binlog dump命令的各种方案，做到开闭原则，对修改封闭，对扩展开放。
     *
     * @see BinlogDumpStrategy
     */
    private BinlogDumpStrategy binlogDumpStrategy;

    /**
     * 加载、写入同步点
     * <p/>
     * 保存最后一个处理的mysql 事件所对应的gt id,重启或者数据源崩溃时从该id继续恢复数据读取。
     *
     * @see DisposeEventPosition
     */
    private DisposeEventPosition disposeEventPosition;

    /**
     * consumer用于保存同步点的桥接器
     *
     * @see DisposeEventPositionBridge
     */
    private DisposeEventPositionBridge bridge;

    /**
     * 默认构造方法
     */
    private BinlogSyncBuilder() {
        bridge = new DisposeEventPositionBridgeImpl();
    }

    /**
     * 静态构造方法
     *
     * @return BinlogSyncBuilder
     */
    public static BinlogSyncBuilder newBuilder() {
        return new BinlogSyncBuilder();
    }

    @Override
    public BinlogSyncer build() {
        return new IterableBinlogSyncer(new BinlogSyncerImpl(this, bridge), new ListenerImpl(this, bridge));
    }

    public BinlogSyncBuilder producerName(String producerName) {
        this.producerName = producerName;
        return this;
    }

    public String getProducerName() {
        return producerName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public BinlogSyncBuilder dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public BinlogDumpStrategy getBinlogDumpStrategy() {
        return binlogDumpStrategy;
    }

    public BinlogSyncBuilder binlogDumpStrategy(
            BinlogDumpStrategy binlogDumpStrategy) {
        this.binlogDumpStrategy = binlogDumpStrategy;
        return this;
    }

    public DisposeEventPosition getDisposeEventPosition() {
        return disposeEventPosition;
    }

    public BinlogSyncBuilder disposeEventPosition(DisposeEventPosition disposeEventPosition) {
        this.disposeEventPosition = disposeEventPosition;
        return this;
    }

}
