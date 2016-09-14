package net.neoremind.fountain.zk.test;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.haguard.zk.ZkHaGuard;
import net.neoremind.simplezkclient.SingletonZkClientProvider;

/**
 * 测试leader elector
 *
 * @author zhangxu
 */
public abstract class BaseHaTestCase {

    private final Logger logger = LoggerFactory.getLogger(BaseHaTestCase.class);

    protected abstract String getInstanceName();

    private ZkHaGuard haGurad;

    private SingletonZkClientProvider provider;

    protected String zkString = "127.0.0.1:2181";

    @Before
    public void init() {
        provider = new SingletonZkClientProvider();
        provider.setZookeeperConnectionString(getZkString());

        haGurad = new ZkHaGuard();
        haGurad.setLatchPath("/fountain/beidou/ha");
        haGurad.setZkClientProvider(provider);
        haGurad.init(Thread.currentThread().getName());
    }

    @After
    public void after() {
        haGurad.close();
    }

    private boolean hasLeader() {
        if (haGurad.hasToken()) {
            logger.info(getInstanceName() + " already has token");
            return true;
        }
        if (haGurad.takeTokenWithDefaultTimeout()) {
            logger.info(getInstanceName() + " has no token but get token within wait timeout");
            return true;
        }
        return false;
    }

    public void tryGetLeadership() throws Exception {
        while (true) {
            if (!hasLeader()) {
                Thread.sleep(1000);
                logger.info(getInstanceName() + " can't get token");
            } else {
                Thread.sleep(5000);
                logger.info(getInstanceName() + " get token");
            }
        }
    }

    public String getZkString() {
        return zkString;
    }

}
