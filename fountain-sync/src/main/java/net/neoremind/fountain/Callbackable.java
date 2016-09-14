package net.neoremind.fountain;

/**
 * 可回调的接口
 *
 * @author zhangxu
 */
interface Callbackable<T> {

    /**
     * 回调
     *
     * @param t 利用传入的对象，进行回调操作
     */
    void callback(T t);
}
