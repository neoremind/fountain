package net.neoremind.fountain;

import java.util.NoSuchElementException;

/**
 * 迭代器
 *
 * @author zhangxu
 */
interface Iteration<E> {

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other words, returns <tt>true</tt> if <tt>next</tt>
     * would return an element rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    boolean hasNext();

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     *
     * @throws NoSuchElementException iteration has no more elements.
     */
    E next();

    /**
     * Returns the current element index;
     *
     * @return current element inde
     */
    int currentIndex();
}
