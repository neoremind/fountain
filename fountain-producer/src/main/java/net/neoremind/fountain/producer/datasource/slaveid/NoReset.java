package net.neoremind.fountain.producer.datasource.slaveid;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 初始化后不再重置
 *
 * @author zhangxu
 */
public class NoReset implements Resettable {

    private AtomicBoolean reset = new AtomicBoolean(true);

    @Override
    public boolean isEnableReset() {
        return reset.getAndSet(false);
    }

}
