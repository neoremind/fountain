package net.neoremind.fountain.eventposition.factory;

import net.neoremind.fountain.eventposition.GtIdSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;

public class GtIdSyncPointFactory implements SyncPointFactory {

    @Override
    public SyncPoint factory() {
        return new GtIdSyncPoint();
    }

}
