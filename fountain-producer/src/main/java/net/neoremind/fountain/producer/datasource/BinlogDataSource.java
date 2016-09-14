package net.neoremind.fountain.producer.datasource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * 支持mysql row base binlog的数据源抽象,支持原生的mysql row base binlog和databus
 *
 * @author zhangxu
 */
public interface BinlogDataSource extends MysqlDataSource {

    /**
     * 开启binlog复制
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws TimeoutException
     */
    void openReplication() throws IOException, NoSuchAlgorithmException, TimeoutException;

    /**
     * 是否已经处于复制状态
     *
     * @return
     */
    boolean isOpenReplication();

    /**
     * 读取bin log的一个数据块
     *
     * @return
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    byte[] readEventData() throws IOException, NoSuchAlgorithmException;

    /**
     * 持久化同步点
     *
     * @param point 同步点
     *
     * @return 最终同步点
     */
    SyncPoint persitSyncPoint(SyncPoint point);

    /**
     * 持久化同步点
     *
     * @param point     同步点
     * @param isPersist 是否真的同步
     *
     * @return 最终同步点
     */
    SyncPoint persitSyncPoint(SyncPoint point, boolean isPersist);

    /**
     * 唯一表示本数据源的名字
     *
     * @param name
     */
    void bindUniqName(String name);

    /**
     * 获取当前的mysql binlog point
     *
     * @return BinlogAndOffsetSyncPoint
     *
     * @throws IOException IOException
     */
    BinlogAndOffsetSyncPoint getMasterCurrentEventPosition() throws IOException;

}
