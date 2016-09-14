package net.neoremind.simplezkclient;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * 对外提供单例的CuratorFramework提供者实现，一个实例和zookeeper保持一个连接，
 * 即使多个同步线程同步多库，都会共享这个连接，避免出现有些线程是leader，有些standby的情况。
 *
 * @author zhangxu
 */
public class SingletonZkClientProvider implements ZkClientProvider {

    /**
     * 默认连接zookeeper的重试策略，默认重试1次，每次间隔5s
     */
    private RetryPolicy retryPolicy = new RetryNTimes(1, 5000);

    private CuratorFramework client;

    /**
     * zookeeper连接字符串，例如"127.0.0.1:2181,127.0.0.1:2182"
     */
    private String zookeeperConnectionString;

    /**
     * zookeeper的session timeout，单位毫秒，默认50s。
     * <p/>
     * TODO 可以看做是一个实例挂了，另外一个实例接着消费的时间间隔，也就是所谓的恢复时间。按照测试一般会小于这个时间
     */
    private int sessionTimeoutMs = 50 * 1000;

    /**
     * 连接zookeeper的超时，单位毫秒，默认4s。
     */
    private int connectionTimeoutMs = 4 * 1000;

    @Override
    public void init() {
        CuratorFramework newClient = CuratorFrameworkFactory
                .newClient(zookeeperConnectionString, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
        newClient.start();
        client = newClient;
    }

    @Override
    public void destory() {
        if (null != client) {
            client.close();
        }
    }

    @Override
    public CuratorFramework provideClient() {
        return client;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public String getZookeeperConnectionString() {
        return zookeeperConnectionString;
    }

    public void setZookeeperConnectionString(String zookeeperConnectionString) {
        this.zookeeperConnectionString = zookeeperConnectionString;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }
}
