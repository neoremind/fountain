package net.neoremind.fountain.eventposition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 保存{@link GtIdSyncPoint} 类型syncpoint的实现
 *
 * @author zhangxu
 */
public class LocalFileGtIdSetDisposeEventPosition extends LocalFileDisposeEventPosition {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileGtIdSetDisposeEventPosition.class);

    public LocalFileGtIdSetDisposeEventPosition() {
    }

    public LocalFileGtIdSetDisposeEventPosition(String rootPath) {
        super(rootPath);
    }

    @Override
    protected String getFileExt() {
        return "gtidset";
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected SyncPoint createSyncPoint() {
        return new GtIdSyncPoint();
    }
}
