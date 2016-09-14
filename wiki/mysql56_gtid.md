# MySQL 5.6对接-使用GTID

*下面的例子都可以参考[Fountain-examples](https://github.com/neoremind/fountain/tree/master/fountain-examples/src/test/java/net/neoremind/fountain/examples/inprocess)*


## 1. 使用条件
MySQL 5.6版本之后使用的Global Transaction Id，简称为GtId。GtId由GTID = source_id:transaction_id组成，其中source_id由MySQL server决定，通常就是server的uuid；transaction_id是事务提交到MySQL server的顺序，从1开始编号，依次递增。例如一个GtId如下：
```
3E11FA47-71CA-11E1-9E33-C80AA9429562:23
```

GtIdSet是一批GtId所组成的，用于主从复制，因此从会提交给主一个自己已经同步过的position，因此需要有一个区段，包含开始和结束。目的是为了轻量级的记录大量的连续全局事务ID， 比如已经在从数据库上执行的全局事务ID, 或者主库上正在运行的全局事务ID。这些集合往往包含海量的全局事务ID。

GtIdSet的结构是一个以sidno，通常为server uuid，为序号的数组，每个数组元素都指向一条Interval组成的链表，链表中的每个Interval 用来存放一组事务ID的区段，例如 （1，5）。
```
Gtid_set
| sidno: 1 | -> Interval (1, 5) -> Interval (11, 18)
| sidno: 2 | -> Interval (1, 27) -> Interval (29, 115) -> Interval (117, 129)
| sidno: 3 | -> Interval (1, 154)
```

当没有配置gtidset配置为空时候，使用如下命令获取主库已经执行过的GtIdSet集合，发送intervalEnd（包含）之后的增量。
```
> show global variables like '%gtid_executed%'\G
************************** 1. row ***************************
Variable_name: gtid_executed
Value: 10a27632-a909-11e2-8bc7-0010184e9e08:1-56
```

## 2. Properties配置

### jdbc-mysql.properties
```
# mysql用户名密码
mysql_username=beidou
mysql_password=u7i8o9p0

# 当同步点存储文件不存在时候，使用该配置来进行binlog dump，如果置空，则从MySQL Server最后的点开始
mysql_binlogdump_gtidset=
#mysql_binlogdump_gtidset=3f149314-9cad-11e5-8b30-00259089db03:1-252

# 主mysql地址，端口，slaveId（与其他slave不能重复，由于主备不会同一时间连，则配置中可以相同）
mysql_shard_0_server=10.94.37.23
mysql_shard_0_port=8769
mysql_shard_0_slaveId=10

# 备mysql
mysql_shard_0_ha1_server=10.94.37.23
mysql_shard_0_ha1_port=8769
mysql_shard_0_ha1_slaveId=10

# 下面是mysql的一些高级设置，一般情况下不建议修改
# replication或者query socket的一些初始化参数
mysql_wait_timeout=999999
mysql_net_write_timeout=240
mysql_net_read_timeout=240
mysql_charset=binary

# 当主mysql长时间接收不到任何event时，切换到另外一个备mysql的超时时间，单位为毫秒，不配置默认60s
mysql_replication_socket_so_timeout=120000

# mysql复制线程的一些基础socket参数，timeout单位为毫秒
#mysql_replication_socket_in_buf=16384
#mysql_replication_socket_out_buf=16384
#mysql_replication_socket_connect_timeout=3000
```

### fountain-config.properties
```
# 同步点存储的文件夹地址
producer_position=/Users/baidu/work/fountain/baidu.ares.mysql.rowbase51

# fountain-producer和consumer直接的缓冲队列长度
memq_limit=60000

# 使用MiniTransactionPolicy策略，一个事务中最大处理的行数
trans_max_size=50000

### 按照表名进行过滤时，表名格式为database.table（可以为正则），以逗号分隔
### 当白名单和黑名单同时存在时,只有不在黑名单中同时在白名单中存在的才起作用
#filter_shard_table_black=beidou.*cold
filter_shard_table_white=
filter_shard_table_black=
```

## 3. XML文件配置
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
                <value>classpath:mysql.rowbase56/fountain-config.properties</value>
                <value>classpath:mysql.rowbase56/jdbc-mysql.properties</value>
            </list>
        </property>
    </bean>

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

    <!-- 同步点记录的桥接器，一般用于consumer记录同步点，而不是producer，这样可以保证消费完毕才持久化 -->
    <bean id="disposeEventPositionBridge"
          class="net.neoremind.fountain.eventposition.DisposeEventPositionBridgeImpl"></bean>

    <!-- 记录已处理事件的位置，当fountain重新启动或者切换master时按照如下顺序寻找同步点：
          1）找JVM ThreadLocal获取同步点
          2）通过本地的持久化文件寻找,也就是这里配置的bean可以使用装饰模式来不断的扩展保存、读取同步点的方法，用户可以实现DisposeEventPosition接口，定制自己的存储方式或位置表述
          3）通过本XML配置文件的选项找gtid或者binglogfile+position等信息
          4）调用MySQL的一些query命令来寻找Server最后处理的点 -->
    <bean id="disposeEventPosition" class="net.neoremind.fountain.eventposition.ReadonlyDisposeEventPosition"
          scope="prototype">
        <property name="delegate">
            <bean class="net.neoremind.fountain.eventposition.LocalFileGtIdSetDisposeEventPosition"
                  scope="prototype">
                <property name="rootPath" value="${producer_position}"></property>
                <property name="disposeEventPositionBridge" ref="disposeEventPositionBridge"></property>
            </bean>
        </property>
    </bean>

    <!-- 对于RowsLogEvent的处理策略，MiniTransactionPolicy会按照整个事务积攒一个事务内的所有RowsLogEvent，
         做merge后再通过dispatcher下发到下游 -->
    <bean id="miniransPolicy"
          class="net.neoremind.fountain.producer.dispatch.transcontrol.MiniTransactionPolicy"
          scope="prototype">
        <!-- 事务数据中RowsLogEvent的最大值，配置单位为记录条数，超出这个最大值就丢弃整个事务的数据，缺省为30000 -->
        <property name="maxTransSize" value="${trans_max_size}"></property>
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
         1）BinlogGtIdAresV51DumpStrategy，针对百度自己的MySQL Ares 5.1版本使用gtid
         2）BinlogFileNamePositionDumpStrategy，所有版本MySQL都支持的传统通过binlog filename + position
         3）BinlogGtIdV56DumpStrategy，MySQL5.6之后支持的gtid set dump
         这里注意，默认地isChecksumSupport=false，BinlogGtIdV56DumpStrategy则恒为true，
         对于如果其他版本的MySQL master支持checksum，需要设置isChecksumSupport为true，目前fountain对于4个byte的校验和做忽略处理-->
    <bean id="binlogGtIdV56DumpStrategy"
          class="net.neoremind.fountain.producer.datasource.binlogdump.BinlogGtIdV56DumpStrategy">
        <property name="gtIdset" value="${mysql_binlogdump_gtidset}"/>
        <!--<property name="isChecksumSupport" value="false"/>-->
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
                            <property name="charset" value="${mysql_charset}"/>
                            <property name="userName" value="${mysql_username}"></property>
                            <property name="password" value="${mysql_password}"></property>
                            <property name="soTimeout" value="${mysql_replication_socket_so_timeout}"></property>
                        </bean>
                    </property>
                    <property name="binlogDumpStrategy" ref="binlogGtIdV56DumpStrategy"></property>
                    <property name="slaveId" value="${mysql_shard_0_slaveId}"></property>
                </bean>
                <bean class="net.neoremind.fountain.producer.datasource.MysqlBinlogDataSource">
                    <property name="conf">
                        <bean class="net.neoremind.fountain.datasource.DatasourceConfigure">
                            <property name="mysqlServer" value="${mysql_shard_0_ha1_server}"></property>
                            <property name="mysqlPort" value="${mysql_shard_0_ha1_port}"></property>
                            <property name="waitTimeout" value="${mysql_wait_timeout}"/>
                            <property name="netWriteTimeout" value="${mysql_net_write_timeout}"/>
                            <property name="netReadTimeout" value="${mysql_net_read_timeout}"/>
                            <property name="charset" value="${mysql_charset}"/>
                            <property name="userName" value="${mysql_username}"></property>
                            <property name="password" value="${mysql_password}"></property>
                            <property name="soTimeout" value="${mysql_replication_socket_so_timeout}"></property>
                        </bean>
                    </property>
                    <property name="binlogDumpStrategy" ref="binlogGtIdV56DumpStrategy"></property>
                    <property name="slaveId" value="${mysql_shard_0_ha1_slaveId}"></property>
                </bean>
            </list>
        </property>
    </bean>

    <!-- 监控mysql数据变化的监控器，称之为fountain-producer -->
    <bean id="producer00" class="net.neoremind.fountain.producer.DefaultProducer"
          init-method="start" destroy-method="destroy">
        <!-- 事务控制器,缺省使用net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy,
             这不是代表没事务，而是事务不完整，当一个RowsLogEvent的数据解析完后就下发，
             一个event只是事务中一张表或一张表的部分数据. 一个完成的事务可能是多张表或者全部数据 -->
        <property name="transactionPolicy" ref="miniransPolicy"></property>
        <!-- 绑定要监控的数据源 -->
        <property name="dataSource" ref="groupIdHAMysqlBinlogDataSource00"></property>
        <!-- 数据解析器 -->
        <property name="parser" ref="defaultParser"></property>
        <!-- event匹配器 -->
        <property name="matcher" ref="shardTableMatcher"></property>
        <!-- 配置数据下发,支持多个下发, 每个下发支持不同的下发流程 -->
        <property name="dispatcher" ref="dispatchWorkflow"/>
    </bean>

</beans>
```

如果想使用NonTransactionPolicy策略请修改配置如下：
```
<!-- 对于RowsLogEvent的处理策略，NonTransactionPolicy只要接收到RowsLogEvent，
     便会通过dispatcher下发到下游 -->
<bean id="nonTransactionPolicy"
      class="com.baidu.fountain.producer.dispatch.transcontrol.NonTransactionPolicy"
      scope="prototype">
</bean>
```



