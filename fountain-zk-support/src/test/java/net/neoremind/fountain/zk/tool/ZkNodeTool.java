package net.neoremind.fountain.zk.tool;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.neoremind.simplezkclient.SingletonZkClientProvider;

/**
 * @author zhangxu
 */
public class ZkNodeTool {

    protected String zkString = "127.0.0.1:2181";

    private SingletonZkClientProvider provider;

    @Before
    public void init() {
        provider = new SingletonZkClientProvider();
        provider.setZookeeperConnectionString(zkString);
        provider.init();
    }

    @Test
    public void testGetChildren() throws Exception {
        List<String> children = provider.provideClient().getChildren().forPath("/fountain/beidou/disponse");
        for (String child : children) {
            System.out.println(child);
        }
    }

    @Test
    public void testDelete() throws Exception {
        provider.provideClient().delete().forPath("/fountain/beidou/ha");
        testGetChildren();
    }

}
