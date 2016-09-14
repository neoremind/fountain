package net.neoremind.fountain;

import java.util.List;

import net.neoremind.fountain.common.mq.FountainMQ;
import net.neoremind.fountain.datasource.DatasourceConfigure;
import net.neoremind.fountain.eventposition.AbstractProxyDisposeEventPosition;
import net.neoremind.fountain.eventposition.AsyncFixedRateDisposeEventPosition;
import net.neoremind.fountain.eventposition.DisposeEventPositionBridge;
import net.neoremind.fountain.eventposition.ReadonlyDisposeEventPosition;
import net.neoremind.fountain.eventposition.RegistableDisposeEventPosition;
import net.neoremind.fountain.exception.BinlogSyncInitException;
import net.neoremind.fountain.producer.DefaultProducer;
import net.neoremind.fountain.producer.datasource.BinlogDataSource;
import net.neoremind.fountain.producer.datasource.MysqlBinlogDataSource;
import net.neoremind.fountain.producer.datasource.ha.HAMysqlBinlogDataSource;
import net.neoremind.fountain.producer.datasource.slaveid.RandomSlaveIdGenerateStrategy;
import net.neoremind.fountain.producer.datasource.slaveid.SlaveIdGenerateStrategy;
import net.neoremind.fountain.producer.dispatch.DefaultDispatchWorkflow;
import net.neoremind.fountain.producer.dispatch.DispatchWorkflow;
import net.neoremind.fountain.producer.dispatch.fountainmq.FoutainMQTransport;
import net.neoremind.fountain.producer.parser.impl.DefaultParser;
import net.neoremind.fountain.util.CollectionUtil;

/**
 * Binlog同步器实现
 *
 * @author zhangxu
 */
public class BinlogSyncerImpl implements BinlogSyncer, Callbackable<Listener<FountainMQ>> {

    /**
     * 字符串不为空的条件验证
     */
    private AbstractClosurePredicate<String> stringNotNull;

    /**
     * 基本类型或者装箱类型是否为默认值的条件验证
     */
    private AbstractClosurePredicate primitiveOrWrapperDefaultValue;

    /**
     * slaveId生成策略
     */
    private SlaveIdGenerateStrategy<Integer> slaveIdGenerateStrategy;

    /**
     * consumer用于保存同步点的桥接器
     *
     * @see DisposeEventPositionBridge
     */
    private DisposeEventPositionBridge bridge;

    /**
     * binlog消息增量的处理生成者
     */
    private DefaultProducer producer;

    /**
     * slaveId的范围，用于随机生成策略
     */
    private int[] slaveIdRange = new int[] {100, 5000};

    /**
     * 默认构造方法
     */
    public BinlogSyncerImpl() {
        stringNotNull = new StringNotNullClosurePredicate();
        primitiveOrWrapperDefaultValue =
                new PrimitiveOrWrapperDefaultValueClosurePredicate();
        slaveIdGenerateStrategy = new RandomSlaveIdGenerateStrategy();
        ((RandomSlaveIdGenerateStrategy) slaveIdGenerateStrategy).setStart(slaveIdRange[0]);
        ((RandomSlaveIdGenerateStrategy) slaveIdGenerateStrategy).setEnd(slaveIdRange[1]);
    }

    /**
     * 构造方法
     *
     * @param builder 根据builder构造对象
     * @param bridge  consumer用于保存同步点的桥接器
     */
    public BinlogSyncerImpl(BinlogSyncBuilder builder, DisposeEventPositionBridge bridge) {
        this();
        this.bridge = bridge;
        try {
            Preconditions.checkArgument(builder.getDataSource() != null, "Datasource should not be NULL");
            Preconditions
                    .checkArgument(builder.getBinlogDumpStrategy() != null, "Binlog dump strategy should not be NULL");

            producer = new DefaultProducer();
            producer.setBeanName(Either.or(Defaults.DEFAULT_PRODUCER_NAME).fromNullable(builder.getProducerName()));
            producer.setHaGuard(Beans.HAGUARD.get(builder));
            producer.setTransactionPolicy(Beans.TRANSACTION_POLICY.get(builder));
            producer.setParser(new DefaultParser(Beans.EVENT_MATCHER.get(builder)));
            producer.setMatcher(Beans.EVENT_MATCHER.get(builder));
            producer.setDataSource(newHAMysqlBinlogDataSource(builder));
        } catch (Exception e) {
            throw new BinlogSyncInitException(e.getMessage(), e);
        }
    }

    /**
     * 新建增量消息的分发器，依赖于一个消息队列{@link FountainMQ}
     *
     * @param mq 消息队列，用于生产者和消费者解耦
     *
     * @return 消息的分发器
     */
    private DispatchWorkflow newDispatchWorkflow(FountainMQ mq) {
        FoutainMQTransport mqTransport = new FoutainMQTransport();
        mqTransport.setFmq(mq);
        DefaultDispatchWorkflow workflow = new DefaultDispatchWorkflow();
        workflow.setTranport(mqTransport);
        return workflow;
    }

    /**
     * 新建高可用的MySQL binlog数据源
     *
     * @param builder 根据builder构造对象
     *
     * @return 高可用的MySQL binlog数据源
     */
    private HAMysqlBinlogDataSource newHAMysqlBinlogDataSource(BinlogSyncBuilder builder) {
        HAMysqlBinlogDataSource haDs = new HAMysqlBinlogDataSource();
        List<DataSourceEntity> entities = builder.getDataSource().toEntities();
        List<BinlogDataSource> binlogDataSources = CollectionUtil.createArrayList(entities.size());
        for (DataSourceEntity e : entities) {
            MysqlBinlogDataSource binlogDataSource = new MysqlBinlogDataSource();
            binlogDataSource.setConf(newDatasourceConfigure(e, builder));
            binlogDataSource.setBinlogDumpStrategy(builder.getBinlogDumpStrategy());
            if (primitiveOrWrapperDefaultValue.set(e.getSlaveId()).apply()) {
                binlogDataSource.setSlaveId(e.getSlaveId());
            } else {
                binlogDataSource.setSlaveId(slaveIdGenerateStrategy.get());
            }
            binlogDataSources.add(binlogDataSource);
        }
        AbstractProxyDisposeEventPosition eventPosition = new ReadonlyDisposeEventPosition();
        eventPosition.setDelegate(Beans.DISPOSE_EVENT_POSITION.get(builder));
        if (builder.getDisposeEventPosition() instanceof RegistableDisposeEventPosition) {
            ((RegistableDisposeEventPosition) builder.getDisposeEventPosition()).setDisposeEventPositionBridge(bridge);
        }
        //TODO refactor
        if (builder.getDisposeEventPosition() instanceof AsyncFixedRateDisposeEventPosition) {
            ((AsyncFixedRateDisposeEventPosition) builder.getDisposeEventPosition())
                    .setDisposeEventPositionBridge(bridge);
        }
        haDs.setDisposeEventPosition(eventPosition);
        haDs.setDatasourceChoosePolicy(Beans.DATASOURCE_CHOOSE_POLICY.get(builder));
        haDs.setMysqlDataSourceList(binlogDataSources);
        haDs.init();
        return haDs;
    }

    /**
     * 新建数据源配置
     *
     * @param e       数据源实体
     * @param builder 根据builder构造对象
     *
     * @return 数据源配置
     */
    private DatasourceConfigure newDatasourceConfigure(DataSourceEntity e, BinlogSyncBuilder builder) {
        DatasourceConfigure conf = new DatasourceConfigure();
        conf.setMysqlServer(e.getIp());
        conf.setMysqlPort(e.getPort());
        conf.setUserName(e.getUsername());
        conf.setPassword(e.getPassword());
        if (stringNotNull.set(builder.getCharset()).apply()) {
            conf.setCharset(builder.getCharset());
        }
        if (stringNotNull.set(builder.getDatabaseName()).apply()) {
            conf.setCharset(builder.getDatabaseName());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getSoTimeout()).apply()) {
            conf.setSoTimeout(builder.getSoTimeout());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getConnectTimeout()).apply()) {
            conf.setSoTimeout(builder.getConnectTimeout());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getWaitTimeout()).apply()) {
            conf.setSoTimeout(builder.getWaitTimeout());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getNetReadTimeout()).apply()) {
            conf.setSoTimeout(builder.getNetReadTimeout());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getNetWriteTimeout()).apply()) {
            conf.setSoTimeout(builder.getNetWriteTimeout());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getSendBufferSize()).apply()) {
            conf.setSoTimeout(builder.getSendBufferSize());
        }
        if (primitiveOrWrapperDefaultValue.set(builder.getReceiveBufferSize()).apply()) {
            conf.setSoTimeout(builder.getReceiveBufferSize());
        }
        return conf;
    }

    @Override
    public void callback(Listener<FountainMQ> listener) {
        producer.setDispatcher(newDispatchWorkflow(listener.getQueue()));
    }

    @Override
    public void start() {
        producer.start();
    }

    @Override
    public void stop() {
        producer.destroy();
    }

    public void setBridge(DisposeEventPositionBridge bridge) {
        this.bridge = bridge;
    }
}
