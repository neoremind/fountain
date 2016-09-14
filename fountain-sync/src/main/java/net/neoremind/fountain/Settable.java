package net.neoremind.fountain;

/**
 * 可设置对象的接口
 *
 * @author zhangxu
 */
interface Settable<X, T> {

    /**
     * 设置对象
     *
     * @param t 对象
     *
     * @return 返回被添加该值的对象
     */
    X set(T t);

}
