package net.neoremind.fountain.producer.group;

/**
 * 描述一组producer，简化producer配置
 *
 * @author hexiufeng, zhangxu
 */
public interface ProducerGroup {
    /**
     * 开启一组producer
     */
    void start();

    /**
     * 销毁组
     */
    void destroy();
}
