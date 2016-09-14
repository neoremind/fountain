package net.neoremind.fountain.producer.dispatch.misc;

import java.util.Iterator;

/**
 * 只有一个元素的迭代器
 *
 * @author hexiufeng
 */
public class SingleIterator implements Iterator<Object> {
    private int count = 1;
    private final Object message;

    /**
     * 构造器
     *
     * @param message 消息
     */
    public SingleIterator(Object message) {
        this.message = message;
    }

    @Override
    public boolean hasNext() {
        return count > 0;
    }

    @Override
    public Object next() {
        count--;
        return message;
    }

    @Override
    public void remove() {
        // don't support
    }

}
