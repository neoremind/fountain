# 【高可用】使用Zookeeper做多实例热备以及存储同步点

*下面的例子都可以参考[Fountain-examples](https://github.com/neoremind/fountain/tree/master/fountain-examples/src/test/java/net/neoremind/fountain/examples/inprocess)*

## 1. 背景
* fountain内部支持配置多个数据源做高可用，当一个数据源不可用时，自动切换。但是这仅限于java进程不挂的情况，这里的高可用指的是进程级别的多个实例高可用。
* 多个fountain实例运行，有且仅有一个实例同步MySQL binlog增量，其他实例做standby热备。
* 当fountain实例挂掉（例如java进程被kill或者该实例所在主机网络故障时）时候，可以从分布式存储中获取最近记录成功的同步点，这里采用了zookeeper。
* 由于zookeeper使用Paxos协议做数据一致性保证，因此性能不佳，QPS几百而已，因此fountain实例使用异步化+定时持久化的策略，隔一段时间自动存储同步点，避免打爆Zookeeper。
* 为了避免在切换fountain实例时候，因为slaveId重复，导致同步失败，所以建议使用随机生成slave的策略。
* 另外，正在研发高性能的分布式同步点服务，可以不做定时同步，实现实时同步点同步。

## 2. 依赖配置
需要额外加入如下依赖：

Maven:
```
<dependency>
    <groupId>net.neoremind</groupId>
    <artifactId>fountain-zk-support</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 3. Properties配置

以MySQL5.6用GTID做同步点为例说明。

### jdbc-mysql.properties

```
# mysql用户名密码
mysql_username=beidou
mysql_password=u7i8o9p0

# 当同步点存储文件不存在时候，使用该配置来进行binlog dump，如果置空，则从MySQL Server最后的点开始
mysql_binlogdump_gtidset=

# 主mysql地址，端口，slaveId（与其他slave不能重复，由于主备不会同一时间连，则配置中可以相同）
mysql_shard_0_server=10.94.37.23
mysql_shard_0_port=8769

# 备mysql
mysql_shard_0_ha1_server=10.94.37.23
mysql_shard_0_ha1_port=8769

# 随机slaveId起始和结束区间
mysql_random_slaveId_start=10
mysql_random_slaveId_end=5000

# 下面是mysql的一些高级设置，一般情况下不建议修改
# replication或者query socket的一些初始化参数
mysql_wait_timeout=999999
mysql_net_write_timeout=240
mysql_net_read_timeout=240
mysql_charset=binary

# 当主mysql长时间接收不到任何event时，切换到另外一个备mysql的超时时间，单位为毫秒
mysql_replication_socket_so_timeout=120000

# mysql复制线程的一些基础socket参数，timeout单位为毫秒
#mysql_replication_socket_in_buf=16384
#mysql_replication_socket_out_buf=16384
#mysql_replication_socket_connect_timeout=3000
```

### fountain-config.properties

```
# zookeeper配置
zk_string=127.0.0.1:2181
zk_connection_timeout=4000
zk_session_timeout=50000
# 存储同步点的根路径，叶子节点是instanceName，相当于同步线程的名称，例如producer00，data是同步点
zk_save_position_root_path=/fountain/beidou/eventposition/testha
# 后台异步同步点线程的配置，默认6s延迟启动，20s做一次持久化
async_event_position_thread_initdelay=6000
async_event_position_thread_period=20000
# 多个fountain实力做高可用方案，需要依赖一个leader选举机制，这个超时时间是尝试获取leader latch的毫秒数
# 如果没有获得latch则会阻塞同步线程，然后再次尝试，周而复始，知道leader权转移（一般是down了），其他standby
# 的实例可以竞争获取leader latch，path就是分布式锁的全路径。
zk_leader_latch_timeout=10000
zk_leader_latch_path=/fountain/beidou/leader/testha

# fountain-producer和consumer直接的缓冲队列长度
memq_limit=60000

### 按照表名进行过滤时，表名格式为database.table（可以为正则），以逗号分隔
### 当白名单和黑名单同时存在时,只有不在黑名单中同时在白名单中存在的才起作用
filter_shard_table_white=
filter_shard_table_black=

```

## 4. XML文件配置
### fountain-config.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

    <description>Fountain的Spring配置</description>

    <!-- properties配置文件 -->
    <bean id="producerConfig"
          class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="systemPropertiesModeName" value="SYSTEM_PROPERTIES_MODE_OVERRIDE"/>
        <property name="ignoreResourceNotFound" value="true"/>
        <property name="ignoreUnresolvablePlaceholders" value="true"/>
        <property name="locations">
            <list>
                <value>classpath:zk.ha.baidu.ares.mysql.rowbase51-nontrx/fountain-config.properties</value>
                <value>classpath:zk.ha.baidu.ares.mysql.rowbase51-nontrx/jdbc-mysql.properties</value>
            </list>
        </property>
    </bean>

    <!-- 根据gtid(Global Transaction ID)得到对应的BinlogFileName和Position的扩展器，
         一般用于SyncPoint扩展成binlog dump命名需要的同步点，例如从gtid类型的同步点扩展成binlogFileNameOffset的同步点 -->
    <bean id="groupExtender"
          class="net.neoremind.fountain.producer.datasource.eventpositionext.GtId2BinPositionEventPositionExtender"/>

    <!-- 基于表名的匹配器，支持黑白名单，支持正则表达式。黑名单的表直接忽略，白名单如果置空，则表示不启用，否则必须符合。
         符合表过滤匹配条件的binlog event会被继续处理。可以自己实现EventMatcher接口,定制匹配 -->
    <bean id="shardTableMatcher" class="net.neoremind.fountain.producer.matcher.TableMatcher">
        <property name="tableWhite" value="${filter_shard_table_white}"></property>
        <property name="tableBlack" value="${filter_shard_table_black}"></property>
    </bean>

    <!-- Row based binlog解析器 -->
    <bean id="defaultParser"
          class="net.neoremind.fountain.producer.parser.impl.DefaultParser"
          scope="prototype">
        <constructor-arg ref="shardTableMatcher"/>
    </bean>

    <!-- 对message queue起作用,用于保存在消费时保存gt id,但即使使用其他传输器时也不要删除该对象 -->
    <bean id="disposeEventPositionBridge"
          class="net.neoremind.fountain.eventposition.DisposeEventPositionBridgeImpl"></bean>

    <!-- zookeeper重试策略，参数为重试一次之间的时间间隔毫秒 -->
    <bean id="zkRetryPolicy" class="org.apache.curator.retry.RetryOneTime">
        <constructor-arg value="0"/>
    </bean>

    <!-- zookeeper的单例client，如果不使用ZkHaGuard，那么必须初始化方法填init，否则不会初始化连接zk，这里使用了zkHaGuard，所以不用init-method -->
    <bean id="singletonZkClientProvider" class="net.neoremind.simplezkclient.SingletonZkClientProvider">
        <property name="zookeeperConnectionString" value="${zk_string}"/>
        <property name="retryPolicy" ref="zkRetryPolicy"/>
        <property name="connectionTimeoutMs" value="${zk_connection_timeout}"/>
        <property name="sessionTimeoutMs" value="${zk_session_timeout}"/>
    </bean>

    <bean id="gtIdSyncPointFactory"
          class="net.neoremind.fountain.eventposition.factory.GtIdSyncPointFactory"/>

    <!-- 记录已处理事件的位置，当fountain重新启动或者切换master时会首先找JVM ThreadLocal获取同步点，然后通过本地或者远程服务寻找,
         这里是使用Zookeeper作为同步点处理存取策略，可以使用装饰模式来不断的扩展保存、读取同步点的方法，
         封装一个AsyncFixedRateDisposeEventPosition可以保证不把zk压垮，异步的每隔一段时间自动同步一次，
         保存的根路径在配置中的zkRootPath，叶子节点保存的是instanceName，data为同步点 -->
    <bean id="disposeEventPosition" class="net.neoremind.fountain.eventposition.ReadonlyDisposeEventPosition"
          scope="prototype">
        <property name="delegate">
            <bean class="net.neoremind.fountain.eventposition.AsyncFixedRateDisposeEventPosition" scope="prototype"
                  init-method="init">
                <property name="delegate">
                    <bean class="net.neoremind.fountain.zk.eventposition.ZkDisposeEventPosition" scope="prototype">
                        <property name="zkRootPath" value="${zk_save_position_root_path}"/>
                        <property name="zkClientProvider" ref="singletonZkClientProvider"/>
                        <property name="syncPointFactory" ref="gtIdSyncPointFactory"/>
                    </bean>
                </property>
                <property name="initDelayMs" value="${async_event_position_thread_initdelay}"></property>
                <property name="periodMs" value="${async_event_position_thread_period}"></property>
                <property name="disposeEventPositionBridge" ref="disposeEventPositionBridge"></property>
            </bean>
        </property>
    </bean>

    <!-- 对于RowsLogEvent的处理策略，NonTransactionPolicy只要接收到RowsLogEvent，
         便会通过dispatcher下发到下游 -->
    <bean id="nonTransactionPolicy"
          class="net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy"
          scope="prototype">
    </bean>

    <!--  fountain-producer和consumer直接的缓冲队列 -->
    <bean id="fountainMQ" class="net.neoremind.fountain.common.mq.MultiPermitsMemFountainMQ">
        <constructor-arg index="0">
            <value>${memq_limit}</value>
        </constructor-arg>
    </bean>

    <!-- 通过MQ为通道传输变化的数据传输层 -->
    <bean id="memqTransport"
          class="net.neoremind.fountain.producer.dispatch.fountainmq.FoutainMQTransport">
        <property name="fmq" ref="fountainMQ"/>
    </bean>

    <!-- 变化的数据的下发流程，包括转换、序列化、和传输 -->
    <bean id="dispatchWorkflow"
          class="net.neoremind.fountain.producer.dispatch.DefaultDispatchWorkflow">
        <!-- 转化ChangeDataSet对象到其他的java对象的转化器,用户可以实现EventConverter接口定制转化器,以期转化为对用户更为友好的java对象
             缺省是DefaultEventConverter,它不做任何转化,直接输出ChangeDataSet -->
        <!-- 配置数据传输层 -->
        <property name="tranport" ref="memqTransport"/>
        <!-- <property name="packProtocol" ref="packProtocol"/> -->
        <!-- <property name="transFilter" ref="transFilter"/> -->
    </bean>

    <!-- 真正用于处理消息即变化数据的对象，实现Consumer接口。这一部分涉及使用方的业务逻辑，必须要使用方自行实现，
         如果使用方不使用ConsumerWorkflow接口的缺省实现net.neoremind.fountain.consumer.DefaultConsumerWorkflow, 这部分可以忽略 -->
    <bean id="consumerFromMemMQ" class="net.neoremind.fountain.test.consumer.TestConsumer">
        <property name="bridge" ref="disposeEventPositionBridge"></property>
    </bean>

    <!-- 最终消费、使用变化数据的流程,内置缺省实现是DefaultConsumerWorkflow,它需要反序列化之后调用Consumer接口实现,使用方可以
         通过实现ConsumerWorkflow接口来实现自己的消费流程,此时实现Consumer接口就不是必要的 -->
    <bean id="consumerWorkflowFromMemMQ"
          class="net.neoremind.fountain.consumer.spi.def.DefaultConsumerWorkflow">
        <property name="consumer" ref="consumerFromMemMQ"></property>
        <!-- <property name="recievedDataConverter" ref=""></property> -->
        <!-- <property name="unPackProtocol" ref="unPackProtocol"></property> -->
    </bean>

    <!-- 消费者监听线程 -->
    <bean id="fountainMQMessageListener"
          class="net.neoremind.fountain.consumer.support.fountainmq.FountainMQMessageListener"
          init-method="start" destroy-method="destroy">
        <property name="fmq" ref="fountainMQ"/>
        <property name="workflow" ref="consumerWorkflowFromMemMQ"></property>
    </bean>

    <!-- MySQL binlog dump的策略，针对不同的MySQL版本可以采用如下策略，
         BinlogGtIdAresV51DumpStrategy，针对百度自己的MySQL5.1版本使用gtid
         BinlogFileNamePositionDumpStrategy，所有版本MySQL都支持的传统通过binlog filename + position
         BinlogGtIdV56DumpStrategy，MySQL5.6之后支持的gtid
         这里注意，默认isChecksumSupport=false，BinlogGtIdV56DumpStrategy则恒为true，
         对于如果其他版本的MySQL master支持checksum，需要设置isChecksumSupport为true，目前fountain对于4个byte的校验和做忽略处理-->
    <bean id="binlogGtIdV56DumpStrategy"
          class="net.neoremind.fountain.producer.datasource.binlogdump.BinlogGtIdV56DumpStrategy">
        <property name="gtIdset" value="${mysql_binlogdump_gtidset}"/>
        <!--<property name="isChecksumSupport" value="false"/>-->
    </bean>

    <bean id="randomSlaveIdGenerateStrategy"
          class="net.neoremind.fountain.producer.datasource.slaveid.RandomSlaveIdGenerateStrategy">
        <property name="start" value="${mysql_random_slaveId_start}"></property>
        <property name="end" value="${mysql_random_slaveId_end}"></property>
    </bean>

    <!-- 配置数据源和数据监控器,它们成对出现,数据监控器称之为fountain-producer,每个fountain-producer绑定一个数据源,
         一般数据源是ha数据源。每个fountain-producer是一个线程。如果有多个数据源，可以配置多个配置数据源和数据监控器对 -->
    <!-- 配置需要监控的mysql数据源,支持ha,可以定制多个具体的数据源,一般定制2个,一主一备 -->
    <bean id="groupIdHAMysqlBinlogDataSource00"
          class="net.neoremind.fountain.producer.datasource.ha.HAMysqlBinlogDataSource"
          init-method="init">
        <property name="disposeEventPosition" ref="disposeEventPosition"></property>
        <property name="datasourceChoosePolicy">
            <bean class="net.neoremind.fountain.datasource.RoundRobinDatasourceChoosePolicy">
                <property name="tryInterval" value="3000"></property>
            </bean>
        </property>
        <property name="mysqlDataSourceList">
            <list>
                <bean class="net.neoremind.fountain.producer.datasource.MysqlBinlogDataSource">
                    <property name="conf">
                        <bean class="net.neoremind.fountain.datasource.DatasourceConfigure">
                            <property name="mysqlServer" value="${mysql_shard_0_server}"></property>
                            <property name="mysqlPort" value="${mysql_shard_0_port}"></property>
                            <property name="waitTimeout" value="${mysql_wait_timeout}"/>
                            <property name="netWriteTimeout" value="${mysql_net_write_timeout}"/>
                            <property name="netReadTimeout" value="${mysql_net_read_timeout}"/>
                            <property name="soTimeout" value="${mysql_replication_socket_so_timeout}"/>
                            <property name="charset" value="${mysql_charset}"/>
                            <property name="userName" value="${mysql_username}"></property>
                            <property name="password" value="${mysql_password}"></property>
                        </bean>
                    </property>
                    <property name="binlogDumpStrategy" ref="binlogGtIdV56DumpStrategy"></property>
                    <property name="slaveIdGenerateStrategy" ref="randomSlaveIdGenerateStrategy"></property>
                </bean>
                <bean class="net.neoremind.fountain.producer.datasource.MysqlBinlogDataSource">
                    <property name="conf">
                        <bean class="net.neoremind.fountain.datasource.DatasourceConfigure">
                            <property name="mysqlServer" value="${mysql_shard_0_ha1_server}"></property>
                            <property name="mysqlPort" value="${mysql_shard_0_ha1_port}"></property>
                            <property name="waitTimeout" value="${mysql_wait_timeout}"/>
                            <property name="netWriteTimeout" value="${mysql_net_write_timeout}"/>
                            <property name="netReadTimeout" value="${mysql_net_read_timeout}"/>
                            <property name="soTimeout" value="${mysql_replication_socket_so_timeout}"/>
                            <property name="charset" value="${mysql_charset}"/>
                            <property name="userName" value="${mysql_username}"></property>
                            <property name="password" value="${mysql_password}"></property>
                        </bean>
                    </property>
                    <property name="binlogDumpStrategy" ref="binlogGtIdV56DumpStrategy"></property>
                    <property name="slaveIdGenerateStrategy" ref="randomSlaveIdGenerateStrategy"></property>
                </bean>
            </list>
        </property>
    </bean>

    <!-- 多个fountain实例共存时候，只有一个实例是leader，可以同步增量，其他都是standby热备，使用zk的leader latch实现-->
    <bean id="zkHaGuard" class="net.neoremind.haguard.zk.ZkHaGuard">
        <property name="zkClientProvider" ref="singletonZkClientProvider"></property>
        <property name="defaultTimeoutMs" value="${zk_leader_latch_timeout}"></property>
        <property name="latchPath" value="${zk_leader_latch_path}"></property>
    </bean>

    <!-- 监控mysql数据变化的监控器，称之为fountain-producer -->
    <bean id="producer-zk-ha" class="net.neoremind.fountain.producer.DefaultProducer"
          init-method="start" destroy-method="destroy">
        <!-- 事务控制器,缺省使用net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy,
             这不是代表没事务，而是事务不完整，当一个RowsLogEvent的数据解析完后就下发，
             一个event只是事务中一张表或一张表的部分数据. 一个完成的事务可能是多张表或者全部数据 -->
        <property name="transactionPolicy" ref="nonTransactionPolicy"></property>
        <!-- 绑定要监控的数据源 -->
        <property name="dataSource" ref="groupIdHAMysqlBinlogDataSource00"></property>
        <!-- 数据解析器 -->
        <property name="parser" ref="defaultParser"></property>
        <!-- event匹配器 -->
        <property name="matcher" ref="shardTableMatcher"></property>
        <!-- 配置数据下发,支持多个下发, 每个下发支持不同的下发流程 -->
        <property name="dispatcher" ref="dispatchWorkflow"/>
        <!-- 使用zookeeper来做高可用保证，同一时间只有一个实例是leader，处于同步状态，其他实例都是standby做热备 -->
        <property name="haGuard" ref="zkHaGuard"/>
    </bean>

</beans>
```