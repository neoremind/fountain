package net.neoremind.fountain.eventposition;

import org.apache.commons.lang3.StringUtils;

/**
 * 描述MySQL5.6版本的gtid同步点
 *
 * @author zhangxu
 * @see GtIdSet
 * @see GtId
 */
public class GtIdSyncPoint implements SyncPoint {

    /**
     * gtid集合
     */
    private GtIdSet gtIdSet;

    /**
     * 无参数构造方法
     */
    public GtIdSyncPoint() {

    }

    /**
     * 构造方法 gtid集合
     *
     * @param gtIdSet
     */
    public GtIdSyncPoint(GtIdSet gtIdSet) {
        this.gtIdSet = gtIdSet;
    }

    @Override
    public byte[] toBytes() {
        return gtIdSet.toString().getBytes();
    }

    @Override
    public void parse(byte[] buf) {
        String value = new String(buf);
        value = StringUtils.chomp(value);
        this.gtIdSet = GtIdSet.buildFromString(value);
    }

    @Override
    public String toString() {
        return gtIdSet.toString();
    }

    public GtIdSet getGtIdSet() {
        return gtIdSet;
    }

    public void setGtIdSet(GtIdSet gtIdSet) {
        this.gtIdSet = gtIdSet;
    }

}
