package net.neoremind.fountain;

import java.util.List;

import net.neoremind.fountain.util.CollectionUtil;

/**
 * 列表迭代器实现
 *
 * @author zhangxu
 */
public class ListIteration<T> implements Iteration<T> {

    /**
     * 列表
     */
    protected List<T> list;

    /**
     * 当前列表索引
     */
    protected int index;

    /**
     * 当前数据
     */
    protected T currentData;

    public ListIteration() {
        list = CollectionUtil.createArrayList();
    }

    @Override
    public boolean hasNext() {
        return index < list.size();
    }

    @Override
    public T next() {
        currentData = list.get(index);
        index++;

        return currentData;
    }

    @Override
    public int currentIndex() {
        return index;
    }
}
