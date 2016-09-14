package net.neoremind.haguard;

/**
 * HaGuard抽象实现
 *
 * @author hexiufeng, zhangxu
 */
public abstract class AbstractHaGuard implements HaGuard {

    /**
     * 默认超时时间，默认10s，阻塞fountain主进程的时间
     */
    private long defaultTimeoutMs = 10000;

    @Override
    public boolean takeTokenWithDefaultTimeout() {
        return takeToken(defaultTimeoutMs);
    }

    public long getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public void setDefaultTimeoutMs(long defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
    }
}
