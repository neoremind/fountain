package net.neoremind.fountain.producer;

/**
 * default implement for ConsequentSocketTimeoutHandler, 使用一个超时
 *
 * @author zhangxu
 */
public class DefaultConsequentSocketTimeoutHandler implements
        ConsequentSocketTimeoutHandler {
    /**
     * 用于记录持续超时的时间
     *
     * @author hexiufeng
     */
    private static class TimeRecored {
        long lastTime = Long.MAX_VALUE;
    }

    private final ThreadLocal<TimeRecored> consequentTime = new ThreadLocal<TimeRecored>() {
        @Override
        protected TimeRecored initialValue() {
            return new TimeRecored();
        }
    };

    private int maxConsequentTime = 120000; // ms

    public int getMaxConsequentTime() {
        return maxConsequentTime;
    }

    public void setMaxConsequentTime(int maxConsequentTime) {
        this.maxConsequentTime = maxConsequentTime;
    }

    @Override
    public void clean() {
        if (isNotChanged()) {
            return;
        }
        consequentTime.get().lastTime = Long.MAX_VALUE;
    }

    /**
     * 判断是否还没有记录过socket read time out？
     *
     * @return true, 没有记录过，yes，记录过
     */
    private boolean isNotChanged() {
        return consequentTime.get().lastTime == Long.MAX_VALUE;
    }

    @Override
    public boolean handleTimeout() {
        if (isNotChanged()) {
            consequentTime.get().lastTime = System.currentTimeMillis();
            return false;
        }
        long interval = System.currentTimeMillis() - consequentTime.get().lastTime;

        if (interval >= maxConsequentTime) {
            return true;
        }

        return false;
    }

}