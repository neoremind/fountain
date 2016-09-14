package net.neoremind.fountain.changedata;

import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * 可从追溯binlog来源的数据对象描述
 * 
 * @author hexiufeng
 *
 */
public interface BinlogTraceable {

    /**
     * 同步点
     *
     * @return SyncPoint
     */
    SyncPoint getSyncPoint();

    /**
     * 生产者的实例
     *
     * @return string
     */
    String getInstanceName();
    
    /**
     * 对象的size
     *
     * @return int
     */
    int getDataSize();
}
