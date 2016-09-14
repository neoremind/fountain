package net.neoremind.fountain.consumer.spi;

/**
 * 具体如何消费一个数据的抽象接口
 * 
 * @author hanxu
 * 
 */
public interface Consumer {
    <T> boolean consume(T event);
}
