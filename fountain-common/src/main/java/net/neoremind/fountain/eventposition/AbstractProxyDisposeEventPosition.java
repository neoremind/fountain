package net.neoremind.fountain.eventposition;

/**
 * 抽象的代理DisposeEventPosition
 *
 * @author hexiufeng, zhangxu
 */
public abstract class AbstractProxyDisposeEventPosition implements DisposeEventPosition {

    protected DisposeEventPosition delegate;

    public DisposeEventPosition getDelegate() {
        return delegate;
    }

    public void setDelegate(DisposeEventPosition delegate) {
        this.delegate = delegate;
    }

    @Override
    public void registerInstance(String insName) {
        delegate.registerInstance(insName);
    }

    @Override
    public SyncPoint loadSyncPoint() {
        return delegate.loadSyncPoint();
    }

}
