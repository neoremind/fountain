package net.neoremind.fountain.eventposition.factory;

import net.neoremind.fountain.eventposition.BaiduGroupIdSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * 生成BaiduGroupIdSyncPoint
 * 
 * @author hexiufeng
 *
 */
public class BaiduGroupIdSyncPointFactory implements SyncPointFactory {

    @Override
    public SyncPoint factory() {
        return new BaiduGroupIdSyncPoint();
    }

}
