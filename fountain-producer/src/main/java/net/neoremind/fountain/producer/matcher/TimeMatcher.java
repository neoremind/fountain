package net.neoremind.fountain.producer.matcher;

import net.neoremind.fountain.event.BaseLogEvent;

/**
 * @author hanxu
 */
@Deprecated
public class TimeMatcher implements EventMatcher {
    private long minTimeMills = -1;
    private long maxTimeMills = -1;

    @Override
    public boolean matcher(BaseLogEvent event) {

        long currentTimeMills = event.getEventHeader().getTimestamp() * 1000;
        if (minTimeMills > 0 && currentTimeMills < minTimeMills) {
            return false;
        }

        if (maxTimeMills > 0 && currentTimeMills > maxTimeMills) {
            return false;
        }

        return true;
    }

    public long getMinTimeMills() {
        return minTimeMills;
    }

    public void setMinTimeMills(long minTimeMills) {
        this.minTimeMills = minTimeMills;
    }

    public long getMaxTimeMills() {
        return maxTimeMills;
    }

    public void setMaxTimeMills(long maxTimeMills) {
        this.maxTimeMills = maxTimeMills;
    }
}
