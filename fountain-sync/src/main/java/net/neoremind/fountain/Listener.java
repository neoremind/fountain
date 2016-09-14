package net.neoremind.fountain;

/**
 * 使用队列做消息获取的监听器
 *
 * @author zhangxu
 */
interface Listener<T> extends Queueable<T> {

    /**
     * 启动
     */
    void start();

    /**
     * 销毁
     */
    void destroy();

}
