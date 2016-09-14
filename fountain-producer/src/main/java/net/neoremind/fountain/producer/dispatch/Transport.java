package net.neoremind.fountain.producer.dispatch;

/**
 * 传输层抽象,将变化的数据发送出去
 *
 * @author hexiufeng
 */
public interface Transport {
    /**
     * 真正下发传输对象
     *
     * @param event 事件
     *
     * @throws DispatchException DispatchException
     */
    void transport(Object event) throws DispatchException;

    /**
     * 真正下发传输对象
     *
     * @param event    事件
     * @param mustSend 是否必须成功发送
     *
     * @throws DispatchException DispatchException
     */
    public void transport(Object event, boolean mustSend)
            throws DispatchException;

    /**
     * 注册生产者，指该传输对象属于哪个生产者；
     *
     * @param producer producer
     */
    void registerProducer(String producer);
}
