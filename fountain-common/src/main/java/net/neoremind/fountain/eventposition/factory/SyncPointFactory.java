package net.neoremind.fountain.eventposition.factory;

import net.neoremind.fountain.eventposition.SyncPoint;

/**
 * 生成SyncPoint对象的工厂
 * 
 * @author hexiufeng
 *
 */
public interface SyncPointFactory {
    /**
     * 生成SyncPoint
     *
     * @return SyncPoint
     */
    SyncPoint factory();
}
