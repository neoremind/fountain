package net.neoremind.fountain.eventposition;

/**
 * 不用延迟直接存储GtId，针对MySQL5.6本身会Server，fountain会传递自己的executed gtid set，以{@link GtIdSet}中的{@link GtId#intervalStart}和
 * {@link GtId#intervalEnd}表示，这里面{@link GtId#intervalEnd}是个开区间，最后一个点会重新下发。
 *
 * @author zhangxu
 * @see GtIdSyncPoint
 */
public class DelayGtIdDisposeEventPosition extends AbstractProxyDisposeEventPosition implements DisposeEventPosition {

    private DisposeEventPositionBridge disposeEventPositionBridge;

    @Override
    public void saveSyncPoint(SyncPoint point) {
        if (!(point instanceof GtIdSyncPoint)) {
            throw new RuntimeException("EventPosition only supports GtIdSyncPoint.");
        }
        delegate.saveSyncPoint(point);
    }

    @Override
    public void registerInstance(String insName) {
        super.registerInstance(insName);
        if (disposeEventPositionBridge != null) {
            disposeEventPositionBridge.register(insName, this);
        }
    }

    public void setDisposeEventPositionBridge(DisposeEventPositionBridge disposeEventPositionBridge) {
        this.disposeEventPositionBridge = disposeEventPositionBridge;
    }

}
