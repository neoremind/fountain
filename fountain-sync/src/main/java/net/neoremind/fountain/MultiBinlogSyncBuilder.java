package net.neoremind.fountain;

import java.util.List;

import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridgeImpl;
import net.neoremind.fountain.util.CollectionUtil;

/**
 * 多分片数据源的binlog同步器builder
 *
 * @author zhangxu
 */
public class MultiBinlogSyncBuilder extends BinlogSyncBuilderTemplate implements Builder<BinlogSyncer> {

    /**
     * 多分片数据源列表
     */
    private List<BinlogSyncer> binlogSyncerList;

    /**
     * consumer用于保存同步点的桥接器
     *
     * @see DisposeEventPositionBridge
     */
    private DisposeEventPositionBridge bridge;

    /**
     * 构造方法
     */
    public MultiBinlogSyncBuilder() {
        this.binlogSyncerList = CollectionUtil.createArrayList(8);
        bridge = new DisposeEventPositionBridgeImpl();
    }

    /**
     * 静态构造方法
     *
     * @return BinlogSyncBuilder
     */
    public static MultiBinlogSyncBuilder newBuilder() {
        return new MultiBinlogSyncBuilder();
    }

    /**
     * 加入单分片数据源同步器的链式调用方法
     *
     * @param syncer 单分片数据源同步器
     *
     * @return MultiBinlogSyncBuilder
     */
    public MultiBinlogSyncBuilder add(BinlogSyncer syncer) {
        if (syncer instanceof BinlogSyncerImpl) {
            ((BinlogSyncerImpl) syncer).setBridge(bridge);
        }
        this.binlogSyncerList.add(syncer);
        return this;
    }

    @Override
    public BinlogSyncer build() {
        return new IterableBinlogSyncer(binlogSyncerList, new ListenerImpl(this, bridge));
    }
}
