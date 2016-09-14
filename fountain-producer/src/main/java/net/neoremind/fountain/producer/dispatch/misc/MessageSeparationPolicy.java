package net.neoremind.fountain.producer.dispatch.misc;

import java.util.Iterator;

/**
 * 消息的分割策略，一个消息可能太大必须被分成成多个进行分发
 *
 * @author hexiufeng
 */
public interface MessageSeparationPolicy {
    /**
     * 分割消息
     *
     * @param message 原来的消息
     *
     * @return 被分割出来的消息
     */
    Iterator<Object> separate(final Object message);
}
