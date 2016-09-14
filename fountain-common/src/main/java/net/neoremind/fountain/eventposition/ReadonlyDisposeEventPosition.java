package net.neoremind.fountain.eventposition;

/**
 * 用于fountain-producer和fountaion-consumer在同一jvm中，producer使用该类只读取文件中 存储的gt id，
 * 而不会记录gt id，记录gt id的行为交给consumer实现。
 * 它直接封装并委托读操作给{@link DisposeEventPosition DisposeEventPosition}
 *
 * @author hexiufeng
 */
public class ReadonlyDisposeEventPosition extends AbstractProxyDisposeEventPosition implements DisposeEventPosition {

    @Override
    public void saveSyncPoint(SyncPoint point) {
        // do nothing
    }
}
