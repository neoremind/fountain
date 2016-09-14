package net.neoremind.fountain.zk.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.curator.retry.RetryOneTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.simplezkclient.SingletonZkClientProvider;
import net.neoremind.fountain.eventposition.BaiduGroupIdSyncPoint;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.eventposition.factory.BaiduGroupIdSyncPointFactory;
import net.neoremind.fountain.zk.eventposition.ZkDisposeEventPosition;

import junit.framework.Assert;

/**
 * @author zhangxu
 */
public class ZkDisponseEventPositionTest {

    private static final Logger logger = LoggerFactory.getLogger(ZkDisponseEventPositionTest.class);

    private ZkDisposeEventPosition zkDisposeEventPosition = new ZkDisposeEventPosition();

    private SingletonZkClientProvider provider;

    protected String zkString = "127.0.0.1:2181";

    @Before
    public void before() throws Exception {
        provider = new SingletonZkClientProvider();
        provider.setZookeeperConnectionString(zkString);
        provider.setRetryPolicy(new RetryOneTime(0));
        provider.init();
        zkDisposeEventPosition.setZkClientProvider(provider);
        zkDisposeEventPosition.setZkRootPath("/fountain/beidou/eventposition");
        zkDisposeEventPosition.registerInstance("test00");
        zkDisposeEventPosition.setSyncPointFactory(new BaiduGroupIdSyncPointFactory());
    }

    @After
    public void after() {
        provider.destory();
    }

    @Test
    public void testSaveAndLoad() {
        logger.info("Ready to save point " + 38988L);
        SyncPoint newSyncPoint = new BaiduGroupIdSyncPoint(BigInteger.valueOf(38988L));
        zkDisposeEventPosition.saveSyncPoint(newSyncPoint);
        SyncPoint loadSyncPoint = zkDisposeEventPosition.loadSyncPoint();
        assertThat(ByteBuffer.wrap(newSyncPoint.toBytes()).equals(ByteBuffer.wrap(loadSyncPoint.toBytes())), is(true));
    }

    @Test
    public void testSaveAndLoadLoop() {
        for (int i = 0; i < 100; i++) {
            try {
                logger.info("Ready to save point " + i);
                SyncPoint newSyncPoint = new BaiduGroupIdSyncPoint(BigInteger.valueOf(i));
                zkDisposeEventPosition.saveSyncPoint(newSyncPoint);
                SyncPoint loadSyncPoint = zkDisposeEventPosition.loadSyncPoint();
                System.out.println(loadSyncPoint);
                Thread.sleep(3000L);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 测试结果是42s 5000次，zk基本的QPS是100.
     */
    @Test
    public void savePerformence() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 5000; i++) {
            BaiduGroupIdSyncPoint point = new BaiduGroupIdSyncPoint(BigInteger.valueOf(3000L + i));
            zkDisposeEventPosition.saveSyncPoint(point);
        }
        long end = System.currentTimeMillis();

        System.out.println(end - start);
        BaiduGroupIdSyncPoint readPoint = (BaiduGroupIdSyncPoint) zkDisposeEventPosition.loadSyncPoint();
        Assert.assertTrue(readPoint.getGroupId().longValue() == 7999L);
    }
}
