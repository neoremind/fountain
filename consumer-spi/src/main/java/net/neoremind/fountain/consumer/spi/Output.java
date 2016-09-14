package net.neoremind.fountain.consumer.spi;

/**
 * 输出数据到mq的抽象
 *
 * @author hexiufeng
 */
public interface Output {
    /**
     * 输出数据
     *
     * @param message，可能是单条数据，也可能是一包数据包
     *
     * @return boolean
     */
    boolean output(Object message);
}
