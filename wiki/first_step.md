# 开发接入步骤

*下面的例子都可以参考[Fountain-examples](https://github.com/neoremind/fountain/tree/master/fountain-examples/src/test/java/net/neoremind/fountain/examples/inprocess)*

## 1. 可扩展消费者配置
开发一个消费MySQL增量的Consumer实现，举例如下，该消费者接到ChangeDataSet后，先打印表结构，然后打印增量数据，最后在本地持久化记录同步点文件。

```
public class TestConsumer implements Consumer {

    private Logger logger = LoggerFactory.getLogger(TestConsumer.class);

    /**
     * 桥接的同步点
     */
    private DisposeEventPositionBridge bridge;

    @Override
    public <T> boolean consume(T event) {
        ChangeDataSet ds = (ChangeDataSet) event;
        logger.info("Consumer receive: " + ds);
        savePoint(ds);
        return true;
    }

    /**
     * 持久化保存同步点
     *
     * @param ds 数据变化
     */
    private void savePoint(ChangeDataSet ds) {
        if (bridge != null) {
            bridge.getDisposeEventPosition(ds.getInstanceName()).saveSyncPoint(ds.getSyncPoint());
        }
    }

    public void setBridge(DisposeEventPositionBridge bridge) {
        this.bridge = bridge;
    }

}
```

其中ChangeDataSet就是增量对象，表示一次数据变化。类图如下，这个可扩展消费者可以利用这个对象转化构造为下游需要的其他对象，输出可以是本地文件也可以是MQ等。

![](http://neoremind.com/wp-content/uploads/2016/09/changedataset.png)

## 2. 生产者消费者配置

下面的说明包含properties以及XML全部的配置项目说明，不同的MySQL版本、以及不同的binlog dump策略的具体使用步骤请参考

* [MySQL 5.6对接-使用GTID](mysql56_gtid.md)

* [MySQL 5.X对接-使用binlogname+position](mysql_binlogname_position.md)

下面详细描述XML中具体的配置项的说明。
### TableMatcher
* 按照表名匹配数据是否可以被继续处理的匹配器，如果不能被匹配成功，消息数据会被丢弃。
* 他们支持白名单和黑名单，匹配规则如下：
* 用户既没有配置白名单也没有配置黑名单，所有数据将被继续处理
* 表名一旦被黑名单匹配，数据则被直接丢弃；
* 表名被白名单匹配，数据将被继续处理，所有不在白名单里面的即会丢弃
* 用户配置了黑名单或者白名单，但表名都不能被匹配，数据将被丢弃
* 表名匹配支持正则表达式，多个匹配规则以逗号分隔

### DisposeEventPosition
记录同步的工具，有多种实现，以存储介质的方式划分：
* 存储在本地磁盘文件的实现，LocalFileDisposeEventPosition或者其继承类，可以使用装饰模式封装。
* 存储在zookeeper的实现，ZkDisposeEventPosition。

按照功能实现来划分：
* 存储控制实现，不能直接存储同步点，使用代理模式控制如何存储。
* ReadonlyDisposeEventPosition，只能读取同步点，但不能存储同步点

### DisposeEventPositionBridge
在"消费端"获取记录数据所属同步点工具实例的"桥"。

这里的"消费端"指的是内存缓冲区数据的消费端。DisposeEventPositionBridge适用于"消费端"消费数据后记录同步点的场景，此时会配合ReadonlyDisposeEventPosition使用。

所谓的"桥"其实是一个容器，它容纳着每一个producer实例的同步点工具，内部已map存储，key是producer实例的名字，value是同步点记录工具的实例（DisposeEventPosition的实例），每一producer启动时它会把自己注册给DisposeEventPositionBridge实例，在一个Fountain实例中，不论有多少个producer实现，DisposeEventPositionBridge实例只有一个。

Fountain从binlog中解析出的每一条数据都记录他来自哪个producer实例的实例名，"消费端"根据producer名字从DisposeEventPositionBridge读取DisposeEventPosition记录同步点。

它在扩展Consumer的场景中十分有用。

### FountainMQ
Fountain内部的内存缓冲区。线程安全，可以被多个线程push数据，也可以被多个线程pop数据，内部是一个BlockingQueue。为防止内存被打爆，内存缓冲区有容量控制：一是容量有上限，二是放入缓冲区的数据所占的容量是不同的，可以用来精确控制。内部缓冲区有2中实现：

* MemFountainMQ，有上限控制，但不能精确控制每个放入缓冲区的数据所占的容量
* MultiPermitsMemFountainMQ，有上限控制，同时能精确控制每个放入缓冲区的数据所占的容量。使用该内存缓冲区和多producer
  时要注意，太多的数据可能被"饿死":大的数据试图获取一个大的容量权限，但持续有小的数据到来，导致大数据始终不能获取想要的容量授权。


### FoutainMQTransport
通过内存缓冲区传递数据的实现，和FountainMQ配合使用，FoutainMQTransport控制向FountainMQ push/pop数据。

### MessageSeparationPolicy
数据拆分策略，和FoutainMQTransport配合使用，缺省不拆分数据。MessageSeparationPolicy是为了解决上面提到的大数据可能被"饿死"问题，BigChangeDataSetMessageSeparationPolicy实现可以把大的数据拆分成多个小的数据。


### DefaultDispatchWorkflow

缺省的数据分发流程控制实现，producer从mysql获取日志并解析完成后需要通过DefaultDispatchWorkflow "分发"出去，这里的"分发"表示把数据放入内存缓冲区。Fountain推荐直接使用DefaultDispatchWorkflow和FoutainMQTransport, 不推荐其他的定制方式，需要需要定制请扩展Consumer。

### FountainMQMessageListener
对接FoutainMQTransport，从其内部的内存缓冲区自动监听数据并交由数据消费处理流程DefaultConsumerWorkflow处理。


### DefaultConsumerWorkflow
"消费"处理流程缺省实现。它实现ConsumerWorkflow接口，Fountain不推荐定制ConsumerWorkflow。如果有特殊处理请扩展Consumer。

* DefaultConsumerWorkflow可用于在producer实例中直接消费数据的场景
* 也可以用于从一个物理的MQ（比如bigpipe）获取数据后消费。Fountain没有封装从物理MQ读取数据的逻辑，只是规范了消费的逻辑。至于从物理MQ读取数据的逻辑必须有Fountain的使用方针对其使用的MQ的客户端api进行封装。

### Consumer
具体消费数据的抽象接口，Fountain的使用方必须实现该接口以便实现其应用场景。上述的一个可扩展消费者的例子就是一个Consumer的实现。

对于在producer实例中直接消费数据的场景请注意，请在Consumer实现中注入DisposeEventPositionBridge并在消费完成数据后记录同步点。

### BinlogDumpStrategy
MySQL binlog dump的策略，针对不同的MySQL版本可以采用如下策略，
* BinlogGtIdAresV51DumpStrategy，针对百度自己的MySQL Ares 5.1版本使用gtid
* BinlogFileNamePositionDumpStrategy，所有版本MySQL都支持的传统通过binlog filename + position
* BinlogGtIdV56DumpStrategy，MySQL5.6之后支持的gtid set dump
  这里注意，默认地isChecksumSupport=false，BinlogGtIdV56DumpStrategy则恒为true，对于如果其他版本的MySQL master支持checksum，需要设置isChecksumSupport为true，目前fountain对于4个byte的校验和做忽略处理。

### MysqlBinlogDataSource/DatabusGtDataSource/DatabusPositionDataSource
mysql数据源的抽象。
* MysqlBinlogDataSource基于原生rowbase binlog数据源的实现
* DatasourceConfigure 数据源配置描述

### HAMysqlBinlogDataSource
多mysql数据源的抽象描述，用于控制mysql的故障转移。
* mysql出现故障并且socket读取出现异常，Fountain会切换数据源
* 当使用从socket读取数据，但超时后依然无法读取数据，Fountain也会切换数据源
* Fountain切换数据源时，它首先会重连一次当前数据源，确认无法连接成功后才切换另一数据源


| #    | 属性                  | 说明                           | 缺省值   |
| ---- | ------------------- | ---------------------------- | ----- |
| 1    | mysqlDataSourceList | 多个备用数据源                      |       |
| 2    | ioRetryCnt          | 在当前mysql数据源执行query出现异常时，重试次数 | 2     |
| 3    | autoMonitor         | 是否需要监控线程监控mysql的日志点          | FALSE |
| 4    | monitorPeriod       | 监控周期，单位分钟                    | 5     |
| 5    | monitorInitialDelay | 监控线程启动后多久开始执行第一次监控，单位分钟      | 5     |


### TransactionPolicy
事务控制策略描述
* NonTransactionPolicy，不需要事务控制，在纯innodb并且业务不需要事务时，推荐使用，性能很高
* MiniTransactionPolicy，小事务控制策略实现。"小"是个相对的概念，最终要表达的意思是，使用内存缓存事务的数据而不能打爆内存。需要Fountain
  的使用者估算一个事务内数据的条数及所占内存的大小，注意要和堆内存大小一起思考，以保证java内存不会被打爆。推荐设置事务数据条数上限，当超过上限时，Fountain会丢弃本事务的数据并记录日志。这种策略如果启用fountain不会积攒一个事务的全部event再下发，而是受到一批发一批。

### DefaultProucer
Fountain数据producer描述，分别对应rowbase

### DefaultProducerGroup
针对分表分库的情况，简化Fountain producer的配置。

## 附录
### 1. 寻找同步点的策略

按照如下优先级寻找SyncPoint。

1）找JVM ThreadLocal获取同步点，一旦fountain运行起来，没下发dispatch一个ChangeDataSet，都会在本线程内保存一个SyncPoint，一旦以为网络原因或者长时间接收不到增量socket超时异常，都会根据这个点重新发送binglog dump命令给MySQL，继续从这个点订阅增量。

2）通过本地的持久化文件寻找,也就是这里配置的bean可以使用装饰模式来不断的扩展保存、读取同步点的方法，用户可以实现DisposeEventPosition接口，定制自己的存储方式或位置表述。
例如：
* 全系列MySQL使用binlogname+position持久化的文件可以为producer00.binlog，里面的内容示例192.168.1.107:3308#mysql-bin
  .000007#618，格式为IP:PORT#binlogfilename#position。
* MySQL5.6版本持久化文件可以为producer00.gtidset，里面的内容示例为：3f149314-9cad-11e5-8b30-00259089db03:1-254，格式为serverUUID:interalStart
  :intervalEnd。

3）通过本XML配置文件的选项找gtid或者binglogfile+position等信息，也就是mysql-jdbc.properties文件中的配置。

4）调用MySQL的一些query命令来寻找Server最后处理的点。比如show master status。




