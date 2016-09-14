package net.neoremind.haguard.zk;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoremind.haguard.AbstractHaGuard;
import net.neoremind.haguard.HaGuard;
import net.neoremind.simplezkclient.ZkClientProvider;
import net.neoremind.util.NetUtils;

/**
 * 基于Zookeeper实现的{@link HaGuard}，使用<tt>curator</tt>框架。
 *
 * @author zhangxu
 */
public class ZkHaGuard extends AbstractHaGuard implements HaGuard, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ZkHaGuard.class);

    private ZkClientProvider zkClientProvider;

    private String latchPath;

    private LeaderLatch leaderLatch;

    private final AtomicReference<State> state = new AtomicReference<State>(State.LATENT);

    /**
     * 阻塞式的初始化和Zookeeper的连接，这个方法在一个容器生命周期中只允许允许一次。
     * <p/>
     * 和zookeeper之间的连接遵循每实例一个ZkClientProvider的方式，这样当实例内部有多个同步线程的时候，
     * 可以共享一个ZkClientProvider，状态都是一致的，避免有些线程是leader，有些是standby的情况。
     * <p/>
     * 改方法是全异步的，如果zookeeper连接不上，也会返回。但是一般上层应用拿不到leader latch，不会成为leader。
     * 而且<code>apache.zookeeper.ClientCnxn</code>包的日志会打印如下：
     * <pre>
     *     Unable to read additional data from server sessionid 0x0, likely server has closed socket, closing socket
     *     connection and attempting reconnect
     * </pre>
     * 参数<code>name</code>标示同步线程的名称，默认的话只有一个可以初始化好和zk的连接，其他的都不再重复初始化了。
     */
    @Override
    public void init(String name) {
        if (!state.compareAndSet(State.LATENT, State.STARTED)) {
            logger.debug("ZkHaGuard can only be initialized once because LeaderLatch should be singleton");
            return;
        }

        if (zkClientProvider == null) {
            throw new IllegalStateException("ZkClientProvider should not be null");
        }
        logger.info("LeaderLatch will start soon by " + name);
        zkClientProvider.init();
        leaderLatch = new LeaderLatch(zkClientProvider.provideClient(), latchPath, NetUtils.getLocalHostIP());
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                logger.info("This instance is leader");
            }

            @Override
            public void notLeader() {
                logger.warn("This instance is NOT leader");
            }
        });
        try {
            leaderLatch.start();
        } catch (Exception e) {
            throw new RuntimeException("Leader latch init failed", e);
        }
        logger.info("LeaderLatch starts by " + name + " asynchronously");
    }

    @Override
    public void close() {
        if (latchPath != null) {
            try {
                leaderLatch.close();
            } catch (IOException e) {
                throw new RuntimeException("Leader latch destroy failed", e);
            }
        }
        if (zkClientProvider != null) {
            zkClientProvider.destory();
        }
        state.compareAndSet(State.STARTED, State.DESTROYED);
    }

    @Override
    public boolean takeToken(long timeout) {
        try {
            return leaderLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean takeTokenWithDefaultTimeout() {
        return takeToken(getDefaultTimeoutMs());
    }

    @Override
    public boolean hasToken() {
        return leaderLatch.hasLeadership();
    }

    public ZkClientProvider getZkClientProvider() {
        return zkClientProvider;
    }

    public void setZkClientProvider(ZkClientProvider zkClientProvider) {
        this.zkClientProvider = zkClientProvider;
    }

    public String getLatchPath() {
        return latchPath;
    }

    public void setLatchPath(String latchPath) {
        this.latchPath = latchPath;
    }

    public enum State {
        LATENT,
        STARTED,
        DESTROYED
    }
}
