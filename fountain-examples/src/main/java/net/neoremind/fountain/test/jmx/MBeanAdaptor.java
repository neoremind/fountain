package net.neoremind.fountain.test.jmx;

/**
 * 定义MBean的适配器接口，MBean是JMX暴露资源的标准结尾，很多开源库均通过该命名标准来找到对应的监控对象，简化程序开发
 * 
 * @author zhangxu
 */
public interface MBeanAdaptor {

    /**
     * 启动MBean适配器服务
     * 
     * @throws Exception
     */
    void start() throws Exception;

    /**
     * 停止MBean适配器服务
     * 
     * @throws Exception
     */
    void stop() throws Exception;

}
