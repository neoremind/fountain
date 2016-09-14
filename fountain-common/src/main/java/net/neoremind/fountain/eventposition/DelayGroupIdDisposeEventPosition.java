package net.neoremind.fountain.eventposition;

/**
 * 延迟一般一个、最多两个事务记录同步点，同步点必须是GroupIdSyncPoint
 *
 * @author hexiufeng
 */
public class DelayGroupIdDisposeEventPosition extends AbstractProxyDisposeEventPosition
        implements DisposeEventPosition {

    private GroupIdSyncPoint lastPoint;

    private DisposeEventPositionBridge disposeEventPositionBridge;

    @Override
    public void saveSyncPoint(SyncPoint point) {
        if (!(point instanceof GroupIdSyncPoint)) {
            throw new RuntimeException("EventPosition only supports GroupIdSyncPoint.");
        }
        GroupIdSyncPoint nowPoint = (GroupIdSyncPoint) point;
        if (isDifferentPoint(nowPoint)) {
            if (lastPoint != null) {
                delegate.saveSyncPoint(lastPoint);
            }
            lastPoint = nowPoint;
        }
    }

    @Override
    public void registerInstance(String insName) {
        super.registerInstance(insName);
        if (disposeEventPositionBridge != null) {
            disposeEventPositionBridge.register(insName, this);
        }
    }

    /**
     * 指定的同步点是否与lastPoint相同
     *
     * @param point 指定的同步点
     *
     * @return true or false
     */
    private boolean isDifferentPoint(GroupIdSyncPoint point) {
        if (lastPoint == null) {
            return true;
        }
        return !lastPoint.isSame(point);
    }

    public void setDisposeEventPositionBridge(DisposeEventPositionBridge disposeEventPositionBridge) {
        this.disposeEventPositionBridge = disposeEventPositionBridge;
    }

}
