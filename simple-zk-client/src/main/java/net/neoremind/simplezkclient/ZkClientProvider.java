package net.neoremind.simplezkclient;

import org.apache.curator.framework.CuratorFramework;

/**
 * 对外提供CuratorFramework的接口描述
 *
 * @author hexiufeng, zhangxu
 */
public interface ZkClientProvider {

    /**
     * 初始化
     */
    void init();

    /**
     * 销毁
     */
    void destory();

    /**
     * 提供CuratorFramework
     *
     * @return CuratorFramework
     */
    CuratorFramework provideClient();

}
