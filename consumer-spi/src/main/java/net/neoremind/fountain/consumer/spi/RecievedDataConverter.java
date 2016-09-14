package net.neoremind.fountain.consumer.spi;

/**
 * 消费者接收后真正消费前可以对数据进行转换
 *
 * @author hexiufeng
 */
public interface RecievedDataConverter {
    /**
     * 转换收到的数据
     *
     * @param dataObject
     *
     * @return 转换后的数据
     */
    Object covert(Object dataObject);
}
