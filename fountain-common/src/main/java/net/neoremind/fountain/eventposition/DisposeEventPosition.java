package net.neoremind.fountain.eventposition;

/**
 * 保存最后一个处理的mysql 事件所对应的gt id,重启或者数据源崩溃时从该id继续恢复数据读取。
 *
 * @author hexiufeng, zhangxu
 */
public interface DisposeEventPosition {
    /**
     * 注册生产者实例的名称,在采用文件保存gt id时会使用该name作为文件名称
     *
     * @param insName producer实例名称
     */
    void registerInstance(String insName);

    /**
     * 加载同步点
     *
     * @return SyncPoint对象
     */
    SyncPoint loadSyncPoint();

    /**
     * 保存同步点
     *
     * @param point SyncPoint
     */
    void saveSyncPoint(SyncPoint point);
}
