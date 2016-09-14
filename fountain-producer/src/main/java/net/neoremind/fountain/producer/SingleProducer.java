package net.neoremind.fountain.producer;

import net.neoremind.fountain.producer.exception.ProducerInitException;

/**
 * 描述单个fountain-producer。具备如下特性:
 * <p/>
 * <ul>
 * <li> 一个fountain-producer针对一个mysql实例进行数据监控</li>
 * <li> 一个fountain-producer是一个线程</li>
 * </ul>
 *
 * @author hexiufeng, zhangxu
 */
public interface SingleProducer {
    /**
     * 该方法会启动一个线程，监控mysql实例并产出变化数据,一般情况下需要使用spring init-method配置该方法
     *
     * @throws ProducerInitException
     */
    void start() throws ProducerInitException;

    /**
     * 最终销毁SingleProducer，释放资源，一般情况下需要使用spring destroy-method配置该方法
     */
    void destroy();
}
