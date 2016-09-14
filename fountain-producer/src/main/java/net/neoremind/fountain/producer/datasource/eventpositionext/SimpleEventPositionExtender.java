package net.neoremind.fountain.producer.datasource.eventpositionext;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.neoremind.fountain.datasource.MysqlDataSource;
import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.EventPositionExtender;
import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * EventPositionExtender简单实现
 *
 * @author hexiufeng
 */
public class SimpleEventPositionExtender implements EventPositionExtender {

    @Override
    public BinlogAndOffsetSyncPoint extend(SyncPoint groupIdPoint,
                                           MysqlDataSource dataSource) throws IOException,
            NoSuchAlgorithmException {
        if (groupIdPoint instanceof BinlogAndOffsetSyncPoint) {
            return (BinlogAndOffsetSyncPoint) groupIdPoint;
        }
        return null;
    }

}
