package net.neoremind.fountain.producer.dispatch.misc;

import java.util.Iterator;

/**
 * 不分割消息实现
 *
 * @author hexiufeng
 */
public class NoSeparationPolicy implements MessageSeparationPolicy {

    @Override
    public Iterator<Object> separate(final Object message) {
        return new SingleIterator(message);
    }

}
