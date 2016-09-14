package net.neoremind.fountain.producer.dispatch;

import net.neoremind.fountain.changedata.ChangeDataSet;

/**
 * 用于发布变化数据的基础流程。fountain会将变化数据从databus或者binlog格式解析为
 * ChangeDataSet类型，ChangeDataSet数据需要通过该流程发送出去，在此流程中也可以
 * 根据自己的需要过滤数据、并把ChangeDataSet转化为自己的java类型，然后通过自己的 打包协议和传输通道发送出去
 *
 * @author hanxu, heiuxfneg
 */
public interface DispatchWorkflow {
    /**
     * 分发变化的数据
     *
     * @param ds 变化数据
     *
     * @throws DispatchException DispatchException
     */
    void dispatchEvent(ChangeDataSet ds) throws DispatchException;

    /**
     * 注册producer
     *
     * @param producer producer
     */
    void registerProducer(String producer);
}
