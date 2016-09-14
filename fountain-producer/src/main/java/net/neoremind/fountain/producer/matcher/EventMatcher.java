package net.neoremind.fountain.producer.matcher;

import net.neoremind.fountain.event.BaseLogEvent;

/**
 * 匹配event是否需要处理，如果匹配成功，则继续处理，否则丢弃event
 *
 * @author hexiufeng
 */
public interface EventMatcher {
    boolean matcher(BaseLogEvent event);
}
