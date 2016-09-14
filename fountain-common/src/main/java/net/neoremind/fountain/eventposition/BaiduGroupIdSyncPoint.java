package net.neoremind.fountain.eventposition;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * 描述百度mysql分支的group id
 *
 * @author hexiufeng, zhangxu
 */
public class BaiduGroupIdSyncPoint implements GroupIdSyncPoint {

    private BigInteger groupId = BigInteger.valueOf(-1);

    /**
     * 无参数构造方法
     */
    public BaiduGroupIdSyncPoint() {

    }

    /**
     * 有参数构造方法
     *
     * @param groupId 事务id
     */
    public BaiduGroupIdSyncPoint(BigInteger groupId) {
        this.groupId = groupId;
    }

    @Override
    public byte[] toBytes() {
        return groupId.toString().getBytes();
    }

    @Override
    public void parse(byte[] buf) {
        String value = new String(buf);
        value = StringUtils.chomp(value);
        groupId = new BigInteger(value);
    }

    public BigInteger getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return groupId.toString();
    }

    @Override
    public BigInteger offerGroupId() {
        return groupId;
    }

    @Override
    public boolean isSame(SyncPoint syncPoint) {
        if (!(syncPoint instanceof BaiduGroupIdSyncPoint)) {
            return false;
        }
        if (syncPoint == this) {
            return true;
        }
        BaiduGroupIdSyncPoint groupPoint = (BaiduGroupIdSyncPoint) syncPoint;
        return groupId.equals(groupPoint.getGroupId());
    }
}
