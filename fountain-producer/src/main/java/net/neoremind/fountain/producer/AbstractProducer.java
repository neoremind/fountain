package net.neoremind.fountain.producer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanNameAware;

import net.neoremind.fountain.changedata.ChangeDataSet;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.haguard.HaGuard;
import net.neoremind.haguard.NoneHaGuard;
import net.neoremind.fountain.meta.CachedTableMetaProvider;
import net.neoremind.fountain.producer.able.Resumable;
import net.neoremind.fountain.producer.able.Suspendable;
import net.neoremind.fountain.producer.datasource.BinlogDataSource;
import net.neoremind.fountain.producer.dispatch.BinlogRowOutputUnitManager;
import net.neoremind.fountain.producer.dispatch.DispatchUnitManager;
import net.neoremind.fountain.producer.dispatch.DispatchWorkflow;
import net.neoremind.fountain.producer.dispatch.transcontrol.TransactionPolicy;
import net.neoremind.fountain.producer.exception.NormalSocketTimeoutException;
import net.neoremind.fountain.producer.exception.ProducerInitException;
import net.neoremind.fountain.producer.matcher.EventMatcher;
import net.neoremind.fountain.producer.parser.Parser;
import net.neoremind.fountain.support.ThreadHolder;
import net.neoremind.fountain.support.TrxContext;
import net.neoremind.fountain.thread.annotaion.UnThreadSafe;
import net.neoremind.fountain.util.UnsignedNumberHelper;

/**
 * {@link SingleProducer SingleProducer}接口的抽象实现，同时实现
 * spring {@link org.springframework.beans.factory.BeanNameAware BeanNameAware}
 * 用于读取spring context中
 * bean的name，这个那么会被自动作为本producer实例的name，用于记录当前同步点的文件name也使用该name。
 * <p>
 * 本抽象类会使用模板方法模式，定制数据接收、抽象、转化、打包、分发的流程。接收数据，解析数据等具体操作也在该 抽象类中实现。
 * </p>
 *
 * @author zhangxu
 */
public abstract class AbstractProducer implements SingleProducer, BeanNameAware, Resumable, Suspendable {

    /**
     * 解析协议数据的解析器
     */
    private Parser parser;

    /**
     * 要监听的数据源，一般是ha数据源
     */
    private BinlogDataSource dataSource;

    /**
     * event数据发送者
     */
    private DispatchWorkflow dispatcher;

    /**
     * 发送单元控制，用于控制每次发送数据的粒度
     */
    protected DispatchUnitManager dispatchUnitManager =
            new BinlogRowOutputUnitManager();

    /**
     * 表粒度过滤器，在从binlog中解析完数据后，如果该表的数据并不是有业务意义的，可被丢弃
     */
    private EventMatcher matcher;

    /**
     * 监控的mysql实例的string的字符集，用于正确的解析string
     */
    private String dbCharset;

    /**
     * 一般只需要一个消费者监控数据变化，其他Standby的作为热备即可，因此这里有一个HA（High Availability）
     * 的守护者（Guard），Guard通过令牌（token）获取监听的许可，只有拿到token的线程才可以监控数据变化。<br/>
     * 默认使用了{@link NoneHaGuard}，直接监控变化，但是在启动多个实例中的线程监听一个MySQL的时候可能会对
     * 下游产生重复消费，例如MQ推消息时候，下游往往不希望重复发送；另外一个问题是如果datasource的slaveId
     * 一样的话，有一个实例是无法正常消费的，因此很多情况下如果使用这个默认的NoneHaGuard，则只能冷备。
     */
    private HaGuard haGuard = new NoneHaGuard();

    /**
     * 下发数据时事务的控制策略
     */
    private TransactionPolicy transactionPolicy;

    /**
     * 当数据源出现异常时下次重新选择新数据源的时间间隔，单位ms
     */
    private int repeatResourceInterval = 5000;

    /**
     * 在开启监控线程时，开启线程的最长时间，如果超过该时间间隔线程还未启动完毕，抛出异常，单位ms
     */
    private int threadStartTimeout = 5000;

    /**
     * 是否已经准备好数据源
     */
    private boolean preparedResource = false;

    /**
     * 监控线程和主线程直接的通信控制
     */
    private final CountDownLatch threadStartWait = new CountDownLatch(1);

    /**
     * 分片名称，指定当前监控的数据源的名称，可以不指定，不指定时使用当前producer实例的beanname
     */
    private String sliceName;

    /**
     * 当前producer实例的beanname
     */
    private String beanName;

    /**
     * 销毁producer时，监控线程和主线程的通信控制器
     */
    private volatile CountDownLatch destroyWait;

    /**
     * 是否shutdown
     */
    private volatile boolean shutDowning = false;

    /**
     * 输出数据处理效率等调优信息
     */
    private boolean enableProfilingPrintInfo = false;

    /**
     * 强制暂停，一般可以配合JMX使用设置该值，阻塞同步线程{@link #threadHandler()}执行
     */
    private volatile boolean forceSuspend = false;

    /**
     * 连续超时控制器,默认实现时立即需要外部切换数据源
     */
    private ConsequentSocketTimeoutHandler consequentSocketTimeoutHandler =
            new ConsequentSocketTimeoutHandler() {

                @Override
                public void clean() {
                }

                @Override
                public boolean handleTimeout() {
                    return true;
                }
            };

    @Override
    public void start() {
        init();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (dbCharset != null) {
                    UnsignedNumberHelper.configCharset(dbCharset);
                }
                registerWorkflow();
                threadStartWait.countDown();
                threadHandler();
            }
        });
        t.setName("fountain-" + this.getInstanceName() + "-" + t.getId());
        t.start();
        try {
            threadStartWait.await(threadStartTimeout, TimeUnit.MILLISECONDS);
            getLogger().info(
                    "Succeed to start producer of " + getInstanceName());
        } catch (InterruptedException e) {
            e.printStackTrace();
            getLogger().error(getInstanceName(), e);
            throw new ProducerInitException();
        }
    }

    /**
     * 初始化对象参数
     */
    private void init() {
        dataSource.bindUniqName(getInstanceName());
        if (transactionPolicy != null) {
            dispatchUnitManager.setTransactionPolicy(transactionPolicy);
        }
        parser.setTableMetaProvider(new CachedTableMetaProvider(dataSource));
        haGuard.init(getInstanceName());
    }

    /**
     * 把当前producer实例名称注册给DispatchWorkflow
     */
    private void registerWorkflow() {
        dispatcher.registerProducer(getInstanceName());
    }

    /**
     * 准备数据源
     */
    private void prepareResource() {
        try {
            dataSource.openReplication();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            getLogger().error(getInstanceName(), e);
            throw new ProducerInitException();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().error(getInstanceName(), e);
            throw new ProducerInitException();
        } catch (TimeoutException e) {
            e.printStackTrace();
            getLogger().error(getInstanceName(), e);
            throw new ProducerInitException();
        }
        dispatchUnitManager.cleanCachedEventData();
        preparedResource = true;
    }

    /**
     * 监控数据处理效率的类，用于统计数据处理过程信息
     */
    @UnThreadSafe
    private static class ProfilingInfo {
        /**
         * 第一条增量处理时间
         */
        long startTime;
        /**
         * 最近一次增量处理时间
         */
        long lastTime;
        /**
         * 需要计算&打印的最小周期
         */
        float intervalTimeInMilli = 60000f;
        /**
         * 历史总增量事件数量
         */
        long totalIncrmentCount;
        /**
         * 计算周期内的增量事件数量
         */
        long incrmentCount;

        void doOne(Logger logger) {
            totalIncrmentCount++;
            incrmentCount++;
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
                lastTime = startTime;
                return;
            }
            long now = System.currentTimeMillis();
            if (now - lastTime > intervalTimeInMilli) {
                float totalQpm = totalIncrmentCount / ((now - startTime) / intervalTimeInMilli);
                float qpm = incrmentCount / ((now - lastTime) / intervalTimeInMilli);
                StringBuilder sb = new StringBuilder();
                sb.append("upTime=");
                sb.append(now - startTime);
                sb.append("ms,totalIncrementCount=");
                sb.append(totalIncrmentCount);
                sb.append(",QPM=");
                sb.append(String.format("%.2f", totalQpm));
                sb.append(". cycleTime=");
                sb.append(now - lastTime);
                sb.append("ms,incrementCount=");
                sb.append(incrmentCount);
                sb.append(",QPM=");
                sb.append(String.format("%.2f", qpm));
                logger.info(sb.toString());
                lastTime = now;
                incrmentCount = 0;
            }
        }
    }

    /**
     * 每线程粒度控制处理效率信息
     */
    private static final ThreadLocal<ProfilingInfo> PROFLING_INFO =
            new ThreadLocal<ProfilingInfo>() {
                @Override
                protected ProfilingInfo initialValue() {
                    return new ProfilingInfo();
                }
            };

    /**
     * 判断当前实例是否是leader，只有leader才能继续监控数据，其他的只能standby
     *
     * @return boolean
     */
    private boolean isHaLeader() {
        if (haGuard.hasToken()) {
            return true;
        }
        if (!haGuard.takeTokenWithDefaultTimeout()) {
            getLogger().warn("Get HA token timeout, current instance will standby and wait for the next try");
            return false;
        }
        return true;
    }

    /**
     * 监控线程的核心处理方法
     */
    private void threadHandler() {
        byte[] data = null;
        BaseLogEvent event = null;
        ThreadHolder.setTrxContext(TrxContext.factory());
        while (true) {
            if (this.forceSuspend) {
                blockThreadHandler();
            }
            if (this.shutDowning) {
                doDestory();
                break;
            }
            if (!isHaLeader()) {
                continue;
            }

            ensureResource();
            // 解析BinlogEvent
            String currentSrcHost = "";
            try {
                data = dataSource.readEventData();
                consequentSocketTimeoutHandler.clean();
                currentSrcHost =
                        dataSource.getIpAddress() + ":" + dataSource.getPort();
            } catch (NormalSocketTimeoutException e) {
                if (consequentSocketTimeoutHandler.handleTimeout()) {
                    getLogger().info("Datasource has no change data for a long time, it should switch to another "
                            + "datasource:{}", combineDatasoruceInfo());
                    realeaseResource();
                } else {
                    getLogger().info(
                            "Datasource has no change data, normal time out:{}",
                            combineDatasoruceInfo());
                }
                continue;
            } catch (RuntimeException e) {
                handleException(data, e);
                continue;
            } catch (Throwable e) {
                handleException(data, e);
                continue;
            }

            try {
                BaseLogEvent[] eventHolder = {null};
                if (!procEventData(data, eventHolder)) {
                    continue;
                } else {
                    event = eventHolder[0];
                }
            } catch (RuntimeException e) {
                handleException(data, e);
                continue;
            } catch (Throwable e) {
                handleException(data, e);
                continue;
            }
            if (this.shutDowning) {
                doDestory();
                break;
            }
            try {
                ThreadHolder.getTrxContext().handleEvent(event);
                ChangeDataSet ds =
                        dispatchUnitManager.accept(event,
                                this.getInstanceName());
                if (ds != null) {
                    ds.setBirthTime(event.getEventHeader().getTimestamp());
                    ds.setSendTime(System.currentTimeMillis());
                    ds.setSrcDbHost(currentSrcHost);
                    fillSyncPoint(ds, event);

                    // 发送前重新验证一下是否具备
                    if (!haGuard.hasToken()) {
                        getLogger().info(
                                "Lost ha token, don't dispatch message.");
                        realeaseResource();
                        continue;
                    }
                    handleDispatch(ds);
                    if (enableProfilingPrintInfo) {
                        PROFLING_INFO.get().doOne(getLogger());
                    }
                }

                endTrans(event);
            } catch (Throwable e) {
                getLogger().error(getInstanceName(), e);
                realeaseResource();
                continue;
            }
        }
    }

    /**
     * 处理协议event数据，如果event包含row data，返回true，并返回事件
     *
     * @param data
     * @param eventHolder 返回事件的占位符
     *
     * @return
     */
    protected abstract boolean procEventData(byte[] data,
                                             BaseLogEvent[] eventHolder);

    /**
     * 当前的logger
     *
     * @return
     */
    protected abstract Logger getLogger();

    /**
     * 判断当前event是否是事务的结束事件，如果是，可以保存gt id
     *
     * @param event
     */
    protected abstract void endTrans(BaseLogEvent event);

    /**
     * 为当前的dataset设置syncPoint属性
     *
     * @param ds dataset
     */
    /**
     * 为当前的dataset设置syncPoint属性
     *
     * @param ds    dataset
     * @param event BaseLogEvent
     */
    protected abstract void fillSyncPoint(final ChangeDataSet ds,
                                          final BaseLogEvent event);

    public DispatchWorkflow getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(DispatchWorkflow dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * 保证可用的数据源，如果数据源出现异常会自动切换到别的数据源， 并且堵塞线程
     */
    private void ensureResource() {
        while (true) {
            if (!preparedResource) {
                try {
                    getLogger().info("Prepare replication socket...");
                    prepareResource();
                } catch (Exception e) {
                    continue;
                }
            } else {
                break;
            }
            if (shutDowning) {
                break;
            }
            try {
                Thread.sleep(repeatResourceInterval);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * 下发数据，如果有一个DispatchWorkflow发送失败，整体发送失败
     *
     * @param ds
     */
    private void handleDispatch(ChangeDataSet ds) {
        this.dispatcher.dispatchEvent(ds);
    }

    /**
     * 处理异常
     *
     * @param data
     * @param e
     */
    protected void handleException(byte[] data, Throwable e) {
        getLogger().error(e.getMessage(), e);
        getLogger().error(combineDatasoruceInfo());

        if (data != null) {
            getLogger().error("Print byte data ... ");
            StringBuffer sb = new StringBuffer();
            for (byte b : data) {
                sb.append(b).append(",");
            }
            getLogger().error(sb.toString());
        }
        realeaseResource();
    }

    /**
     * 获取datasource的描述信息
     *
     * @return datasource info
     */
    private String combineDatasoruceInfo() {
        StringBuffer sb =
                new StringBuffer("datasource is [ip, port] [")
                        .append(dataSource.getIpAddress()).append(", ")
                        .append(dataSource.getPort()).append("]");
        return sb.toString();
    }

    @Override
    public void destroy() {
        destroyWait = new CountDownLatch(1);
        shutDowning = true;
        try {
            destroyWait.await(90, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当遇到destroy事件时释放资源
     */
    private void doDestory() {
        realeaseResource();
        if (destroyWait != null) {
            destroyWait.countDown();
        }
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    protected String getInstanceName() {
        if (null != this.sliceName) {
            return this.sliceName;
        }
        return this.beanName;
    }

    /**
     * 是否当前的数据源资源
     */
    private void realeaseResource() {
        try {
            if (dataSource != null) {
                dataSource.close();
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
        }
        preparedResource = false;
        // 清除该资源上的超时记录
        consequentSocketTimeoutHandler.clean();
    }

    public boolean isEnableProfilingPrintInfo() {
        return enableProfilingPrintInfo;
    }

    public void setEnableProfilingPrintInfo(boolean enableProfilingPrintInfo) {
        this.enableProfilingPrintInfo = enableProfilingPrintInfo;
    }

    public ConsequentSocketTimeoutHandler getConsequentSocketTimeoutHandler() {
        return consequentSocketTimeoutHandler;
    }

    public void setConsequentSocketTimeoutHandler(
            ConsequentSocketTimeoutHandler consequentSocketTimeoutHandler) {
        this.consequentSocketTimeoutHandler = consequentSocketTimeoutHandler;
    }

    public String getSliceName() {
        return sliceName;
    }

    public void setSliceName(String sliceName) {
        this.sliceName = sliceName;
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public BinlogDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(BinlogDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getThreadStartTimeout() {
        return threadStartTimeout;
    }

    public void setThreadStartTimeout(int threadStartTimeout) {
        this.threadStartTimeout = threadStartTimeout;
    }

    public CountDownLatch getThreadStartWait() {
        return threadStartWait;
    }

    public int getRepeatResourceInterval() {
        return repeatResourceInterval;
    }

    public void setRepeatResourceInterval(int repeatResourceInterval) {
        this.repeatResourceInterval = repeatResourceInterval;
    }

    public DispatchUnitManager getDispatchUnitManager() {
        return dispatchUnitManager;
    }

    public void setDispatchUnitManager(DispatchUnitManager dispatchUnitManager) {
        this.dispatchUnitManager = dispatchUnitManager;
    }

    public EventMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(EventMatcher matcher) {
        this.matcher = matcher;
    }

    public String getDbCharset() {
        return dbCharset;
    }

    public void setDbCharset(String dbCharset) {
        this.dbCharset = dbCharset;
    }

    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public void setTransactionPolicy(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }

    public HaGuard getHaGuard() {
        return haGuard;
    }

    public void setHaGuard(HaGuard haGuard) {
        this.haGuard = haGuard;
    }

    public boolean isPreparedResource() {
        return preparedResource;
    }

    @Override
    public void suspend() {
        this.forceSuspend = true;
    }

    @Override
    public synchronized void resume() {
        this.forceSuspend = false;
        notifyAll();
    }

    protected synchronized void blockThreadHandler() {
        try {
            getLogger().warn("Blocking MySQL sync thread since `forceSuspend` is set to true");
            wait();
        } catch (InterruptedException e) {
            getLogger().error(e.getMessage(), e);
        }
    }

}