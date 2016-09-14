package net.neoremind.fountain.test.support;

/**
 * 事件回调
 *
 * @author zhangxu
 */
public interface EventHolderCallback<T> {

    /**
     * 当put到队列中时执行
     *
     * @param t
     */
    void onPut(T t);

}
