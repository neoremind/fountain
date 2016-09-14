package net.neoremind.fountain.eventposition;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.neoremind.fountain.datasource.MysqlDataSource;

/**
 * 扩展已经同步的binlog的位置信息, 一般来讲需要存储binlog event的global transaction id,在使用原生row base binlog 的情况下,需要根据global transaction
 * id转化为binlog file name和position。
 *
 * @author hexiufeng
 */
public interface EventPositionExtender {
    /**
     * 从给定的数据源中读取与global transaction id有关的信息,比如binlog file name和position用于同步
     *
     * @param groupIdPoint 同步点
     * @param dataSource   datasource
     *
     * @return BinlogAndOffsetSyncPoint
     *
     * @throws IOException              读取mysql 可能返回io异常
     * @throws NoSuchAlgorithmException 执行query方法可能返回NoSuchAlgorithmException异常
     */
    BinlogAndOffsetSyncPoint extend(SyncPoint groupIdPoint, MysqlDataSource dataSource) throws IOException,
            NoSuchAlgorithmException;
}
