package net.neoremind.fountain.zk.eventposition;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.simplezkclient.ZkClientProvider;
import net.neoremind.fountain.eventposition.DisposeEventPosition;
import net.neoremind.fountain.eventposition.RegistableDisposeEventPosition;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.eventposition.factory.SyncPointFactory;

/**
 * 基于zookeeper实现的DisposeEventPosition
 * <p/>
 * 由于zookeeper基于Paxos协议，因此性能和并发性能不佳，不能单独直接使用该类，一般需要配合{@link net.neoremind.fountain.eventposition
 * .AsyncFixedRateDisposeEventPosition}来使用，从而可以按照一定的时间间隔存储同步点。
 *
 * @author zhangxu
 */
public class ZkDisposeEventPosition extends RegistableDisposeEventPosition implements DisposeEventPosition {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkDisposeEventPosition.class);

    private ZkClientProvider zkClientProvider;

    /**
     * 用于构造同步点
     */
    private SyncPointFactory syncPointFactory;

    /**
     * zookeeper存储的根节点，一般child节点是同步线程的名称
     */
    private String zkRootPath;

    private boolean isExist = false;

    @Override
    public SyncPoint loadSyncPoint() {
        CuratorFramework client = zkClientProvider.provideClient();
        try {
            if (client.checkExists().forPath(fullPath()) == null) {
                return null;
            }
            byte[] nodeData = client.getData().forPath(fullPath());
            SyncPoint point = syncPointFactory.factory();
            point.parse(nodeData);
            LOGGER.info("Get sync point from zk path {} successfully, value is {}", fullPath(),
                    new String(point.toBytes()));
            return point;
        } catch (KeeperException.NoNodeException e) {
            LOGGER.warn("No sync point found from zk path " + fullPath(), e);
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Load sync point failed from zk path " + fullPath(), e);
        }
    }

    @Override
    public void saveSyncPoint(SyncPoint point) {
        if (point == null) {
            return;
        }
        CuratorFramework client = zkClientProvider.provideClient();
        try {
            if (!isExist) {
                isExist = client.checkExists().forPath(fullPath()) != null;
            }
            if (!isExist) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(fullPath(), point.toBytes());
                isExist = true;
            } else {
                client.setData().forPath(fullPath(), point.toBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("Save sync point failed from zk path " + fullPath(), e);
        }
    }

    private String fullPath() {
        if (zkRootPath.endsWith("\\/")) {
            return zkRootPath + super.getInstanceName();
        }
        return zkRootPath + "/" + super.getInstanceName();
    }

    public void setZkClientProvider(ZkClientProvider zkClientProvider) {
        this.zkClientProvider = zkClientProvider;
    }

    public void setSyncPointFactory(SyncPointFactory syncPointFactory) {
        this.syncPointFactory = syncPointFactory;
    }

    public void setZkRootPath(String zkRootPath) {
        this.zkRootPath = zkRootPath;
    }
}
