package net.neoremind.fountain.eventposition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 保存{@link BinlogAndOffsetSyncPoint} 类型syncpoint的实现
 *
 * @author hexiufeng
 */
public class LocalFileBinlogAndOffsetDisposeEventPosition extends LocalFileDisposeEventPosition {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileBinlogAndOffsetDisposeEventPosition.class);

    public LocalFileBinlogAndOffsetDisposeEventPosition() {
    }

    public LocalFileBinlogAndOffsetDisposeEventPosition(String rootPath) {
        super(rootPath);
    }

    @Override
    protected String getFileExt() {
        return "binlog";
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected SyncPoint createSyncPoint() {
        return new BinlogAndOffsetSyncPoint();
    }
}
