package net.neoremind.fountain.producer.group.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import net.neoremind.fountain.datasource.DatasourceConfigure;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.eventposition.factory.BaiduGroupIdSyncPointFactory;
import net.neoremind.fountain.eventposition.factory.SyncPointFactory;
import net.neoremind.fountain.producer.AbstractProducer;
import net.neoremind.fountain.producer.SingleProducer;
import net.neoremind.fountain.producer.datasource.AbstractMysqlBinlogDataSource;
import net.neoremind.fountain.producer.datasource.BinlogDataSource;
import net.neoremind.fountain.producer.datasource.binlogdump.BinlogDumpStrategy;
import net.neoremind.fountain.producer.datasource.ha.HAMysqlBinlogDataSource;
import net.neoremind.fountain.producer.group.ProducerGroup;

/**
 * ProducerGroup缺省实现,只支持groupId类型的producer.
 * <p/>
 * 它是简化配置的辅助工具。
 * <p/>
 * <p>
 * 一组proudcer支持db源、groupId和slaveId不同，其他属性相同，比如user/pwd
 * </p>
 *
 * @author hexiufeng, zhangxu
 */
public class DefaultProducerGroup implements ProducerGroup, ApplicationContextAware {

    /**
     * db 配置,ip:port,ip:port
     */
    private String dbArray;

    /**
     * 备份db配置
     */
    private String bakDbArray;

    /**
     * 同步点配置,逗号分隔
     */
    private String syncPointArray;

    /**
     * slaveId配置，逗号分隔
     */
    private String slaveIdArray;

    /**
     * 同步点对象生成类厂
     */
    private SyncPointFactory syncPointFactory = new BaiduGroupIdSyncPointFactory();

    private String prototypeProducerBeanName;

    private String prototypeHaDatasourceBeanName;

    private String prototypeDatasourceBeanName;

    private String prototypeBinlogDumpStrategyBeanName;

    /**
     * db 索引号
     */
    private int fromIndex = 0;

    private String prefixName = "producer";

    private ApplicationContext appContext;

    private final List<AbstractProducer> producerList = new ArrayList<AbstractProducer>(8);

    @Override
    public void start() {
        checkGroup();
        int groupSize = calGroupSize();
        for (int i = 0; i < groupSize; i++) {
            producerList.add(buildProducer(i));
        }

        for (SingleProducer producer : producerList) {
            producer.start();
        }
    }

    /**
     * 构建producer
     *
     * @param index 索引
     *
     * @return AbstractProducer
     */
    private AbstractProducer buildProducer(int index) {
        int nameIndex = this.fromIndex + index;
        AbstractProducer producer =
                (AbstractProducer) appContext
                        .getBean(prototypeProducerBeanName);
        producer.setSliceName(prefixName + (nameIndex < 10 ? "0" : "")
                + nameIndex);
        producer.setDataSource(buildDataSource(index));

        return producer;
    }

    /**
     * 构建数据源
     *
     * @param index 索引
     *
     * @return BinlogDataSource
     */
    private BinlogDataSource buildDataSource(int index) {
        HAMysqlBinlogDataSource haDatasource =
                (HAMysqlBinlogDataSource) appContext
                        .getBean(prototypeHaDatasourceBeanName);
        List<BinlogDataSource> datasourceList =
                new ArrayList<BinlogDataSource>();

        AbstractMysqlBinlogDataSource datasource =
                (AbstractMysqlBinlogDataSource) appContext
                        .getBean(prototypeDatasourceBeanName);
        configDatasouce(datasource, index, this.dbArray);
        datasourceList.add(datasource);
        AbstractMysqlBinlogDataSource bakDatasouce =
                (AbstractMysqlBinlogDataSource) appContext
                        .getBean(prototypeDatasourceBeanName);
        configDatasouce(bakDatasouce, index, this.bakDbArray);
        datasourceList.add(bakDatasouce);
        haDatasource.setMysqlDataSourceList(datasourceList);
        haDatasource.init();
        return haDatasource;
    }

    /**
     * 配置数据源
     *
     * @param datasource  数据源
     * @param index       索引
     * @param dbConfArray db信息
     */
    private void configDatasouce(AbstractMysqlBinlogDataSource datasource,
                                 int index, String dbConfArray) {
        DatasourceConfigure conf = datasource.getConf();
        String host = dbConfArray.split(",")[index];
        String point = syncPointArray.split(",")[index];
        SyncPoint syncPoint = syncPointFactory.factory();
        try {
            syncPoint.parse(point.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        BinlogDumpStrategy binlogDumpStrategy = (BinlogDumpStrategy) appContext.getBean(
                prototypeBinlogDumpStrategyBeanName);
        datasource.setBinlogDumpStrategy(binlogDumpStrategy);
        String[] hostArray = host.split(":");
        conf.setMysqlServer(hostArray[0]);
        conf.setMysqlPort(Integer.parseInt(hostArray[1]));
        datasource.apply(syncPoint);
        datasource.setSlaveId(Integer.parseInt(slaveIdArray.split(",")[index]));
    }

    /**
     * 计算组内producer的个数
     *
     * @return producer个数
     */
    private int calGroupSize() {
        return dbArray.split(",").length;
    }

    /**
     * 检查组的配置信息
     */
    private void checkGroup() {
        String errorMsg = "Producer group config error.";
        String[] dbconf = dbArray.split(",");
        String[] syncPointconf = syncPointArray.split(",");
        if (dbconf.length != syncPointconf.length) {
            throw new RuntimeException(errorMsg + " db and syncpoint array length not equal.");
        }
        String[] bakdbconf = this.bakDbArray.split(",");
        if (bakdbconf.length != syncPointconf.length) {
            throw new RuntimeException(errorMsg + " backdb and syncpoint array length not equal.");
        }
        String[] slaveIdConf = slaveIdArray.split(",");
        if (dbconf.length != slaveIdConf.length) {
            throw new RuntimeException(errorMsg + " db and slaveid array length not equal.");
        }
    }

    @Override
    public void destroy() {
        for (SingleProducer producer : producerList) {
            producer.destroy();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        appContext = applicationContext;
    }

    /**
     * 设置db 组
     *
     * @param dbArray 多db逗号分隔
     */
    public void setDbArray(String dbArray) {
        this.dbArray = dbArray;
    }

    /**
     * 设置 ha 环境中备机的db
     *
     * @param bakDbArray 多db逗号分隔
     */
    public void setBakDbArray(String bakDbArray) {
        this.bakDbArray = bakDbArray;
    }

    /**
     * 同步点
     *
     * @param syncPointArray 逗号分隔
     */
    public void setSyncPointArray(String syncPointArray) {
        this.syncPointArray = syncPointArray;
    }

    /**
     * fountain作为slave的id
     *
     * @param slaveIdArray 多个时逗号分隔
     */
    public void setSlaveIdArray(String slaveIdArray) {
        this.slaveIdArray = slaveIdArray;
    }

    /**
     * 解析同步点的类厂
     *
     * @param syncPointFactory 多个时逗号分隔
     */
    public void setSyncPointFactory(SyncPointFactory syncPointFactory) {
        this.syncPointFactory = syncPointFactory;
    }

    /**
     * 设置原型Producer对象对应的spring的bean name
     *
     * @param prototypeProducerBeanName bean name
     */
    public void setPrototypeProducerBeanName(String prototypeProducerBeanName) {
        this.prototypeProducerBeanName = prototypeProducerBeanName;
    }

    /**
     * 设置原型HA 数据源对象对应的spring的bean name
     *
     * @param prototypeHaDatasourceBeanName bean name
     */
    public void setPrototypeHaDatasourceBeanName(
            String prototypeHaDatasourceBeanName) {
        this.prototypeHaDatasourceBeanName = prototypeHaDatasourceBeanName;
    }

    /**
     * 设置原型数据源对象对应的spring的bean name
     *
     * @param prototypeDatasourceBeanName bean name
     */
    public void setPrototypeDatasourceBeanName(
            String prototypeDatasourceBeanName) {
        this.prototypeDatasourceBeanName = prototypeDatasourceBeanName;
    }

    /**
     * 分组中第一个producer对应的序号
     *
     * @param fromIndex 序号
     */
    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    /**
     * Producer对应的名字前缀，prefixName + fromIndex表示完整的名字
     *
     * @param prefixName 名字前缀
     */
    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    /**
     * 设置binlog dump策略
     *
     * @param prototypeBinlogDumpStrategyBeanName
     */
    public void setPrototypeBinlogDumpStrategyBeanName(String prototypeBinlogDumpStrategyBeanName) {
        this.prototypeBinlogDumpStrategyBeanName = prototypeBinlogDumpStrategyBeanName;
    }
}
