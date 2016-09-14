package net.neoremind.fountain.eventposition;

import java.math.BigInteger;

/**
 * 支持groupId SyncPoint
 *
 * @author hexiufeng
 */
public interface GroupIdSyncPoint extends SyncPoint {
    /**
     * 获取groupId
     *
     * @return groupId
     */
    BigInteger offerGroupId();

    /**
     * 和指定的SyncPoint是否相同
     *
     * @param syncPoint SyncPoint
     *
     * @return boolean
     */
    boolean isSame(SyncPoint syncPoint);
}
