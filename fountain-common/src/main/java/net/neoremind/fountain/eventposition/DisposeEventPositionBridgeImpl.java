package net.neoremind.fountain.eventposition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实现{@link DisposeEventPositionBridge DisposeEventPositionBridge}接口。
 * 封装ConcurrentMap对象
 *
 * @author hexiufeng
 */
public class DisposeEventPositionBridgeImpl implements DisposeEventPositionBridge {
    private ConcurrentMap<String, DisposeEventPosition> registMap =
            new ConcurrentHashMap<String, DisposeEventPosition>();

    @Override
    public void register(String producerName, DisposeEventPosition disp) {
        registMap.putIfAbsent(producerName, disp);
    }

    @Override
    public DisposeEventPosition getDisposeEventPosition(String producerName) {
        return registMap.get(producerName);
    }

}
