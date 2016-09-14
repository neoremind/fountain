package net.neoremind.fountain.eventposition;

/**
 * 支持注册的DisposeEventPosition抽象实现
 *
 * @author hexiufeng
 */
public abstract class RegistableDisposeEventPosition implements DisposeEventPosition {

    private DisposeEventPositionBridge disposeEventPositionBridge;
    private String instanceName;

    public String getInstanceName() {
        return instanceName;
    }

    public DisposeEventPositionBridge getDisposeEventPositionBridge() {
        return disposeEventPositionBridge;
    }

    public void setDisposeEventPositionBridge(DisposeEventPositionBridge disposeEventPositionBridge) {
        this.disposeEventPositionBridge = disposeEventPositionBridge;
    }

    @Override
    public void registerInstance(String insName) {
        this.instanceName = insName;
        if (disposeEventPositionBridge != null) {
            disposeEventPositionBridge.register(instanceName, this);
        }
    }

}
