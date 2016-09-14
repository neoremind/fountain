package net.neoremind.fountain.eventposition.factory;

import net.neoremind.fountain.eventposition.BinlogAndOffsetSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * 生成BinlogAndOffsetSyncPoint
 * 
 * @author hexiufeng
 *
 */
public class BinlogAndOffsetSyncPointFactory implements SyncPointFactory {

    @Override
    public SyncPoint factory() {
        return new BinlogAndOffsetSyncPoint();
    }

}
