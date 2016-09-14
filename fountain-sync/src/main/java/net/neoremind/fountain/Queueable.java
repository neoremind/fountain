package net.neoremind.fountain;

/**
 * 可队列化的接口
 *
 * @author zhangxu
 */
interface Queueable<T> {

    /**
     * 返回队列
     *
     * @return 队列
     */
    T getQueue();

}
