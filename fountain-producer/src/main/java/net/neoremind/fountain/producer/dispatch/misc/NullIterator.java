package net.neoremind.fountain.producer.dispatch.misc;

import java.util.Iterator;

/**
 * 没任何元素的迭代器
 *
 * @author hexiufeng
 */
public class NullIterator implements Iterator<Object> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        return null;
    }

    @Override
    public void remove() {
        // do nothing
    }

}
