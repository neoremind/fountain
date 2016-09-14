package net.neoremind.fountain.producer.datasource.ha;

import java.util.ArrayList;
import java.util.List;

import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint.MysqlSyncPoint;

/**
 * 定时监控slave当前binlog 位置的辅助类.
 * <p/>
 * <p>
 * 监控线程和同步线程需要并发访问pointList，所以需要加锁控制
 * </p>
 *
 * @author hexiufeng
 */
public final class MonitorHelper {
    private List<MysqlSyncPoint> pointList = new ArrayList<MysqlSyncPoint>();

    public synchronized List<MysqlSyncPoint> getPointList() {
        List<MysqlSyncPoint> newList = new ArrayList<MysqlSyncPoint>(pointList.size());
        newList.addAll(pointList);
        return newList;
    }

    public synchronized void setPointList(List<MysqlSyncPoint> pointList) {
        this.pointList = pointList;
    }

}
