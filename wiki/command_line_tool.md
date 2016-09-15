# 命令行工具

## 1. 使用方式

### 步骤一：下载工具包
[下载地址]()  //TODO
解压fountain-runner-${version}.tar.gz。

### 步骤二：配置
解压后，包内文件以及作用如下：

```
/fountain-runner-${version}/bin/
                               |- start.sh           //启动脚本
                               |- stop.sh            //停止脚本
                               |- conf.sh            //基础环境配置文件。如果在start.sh后面加入--no-exec-conf-sh参数，则不会从这个文件中读取配置，也包括下面的env.properties，但是需要保证环境变量中存在一切需要的配置。
                               |- env.properties     //fountain程序运行配置文件。文件中的所有变量都会利用export命令set到环境变量中，以便fountain程序通过spring-boot去把环境变量的值覆盖到properties文件中生效。
                               |- export.sh          //勿动。
                           /resource/
                                    |- applicationContext-fountain-runner.xml        //fountain主Spring XML配置
                                    |- fountain-consumer-demo.xml                    //测试demo用的可插拔consumer XML配置
                                    |- log4j.properties                              //输出日志管理
                                    |- baidu.ares.mysql.rowbase51/                   //fountain利用spring做一套代码多套部署用的profile，下面三个文件都是fountain运行必须的配置文件，包括XML以及properties，上面提到的env.properties会覆盖这里面的properties。
                                                                 |- fountain-config.xml
                                                                 |- fountain-config.properties
                                                                 |- jdbc-mysql.properties
                                    |- mysql.rowbase56                               //同上，另外一些fountain的profiles，下同
                                    |- mysql.rowbase56-nontrx
                                    |- ......
                           /lib/                     //所有的lib，jar包存放处，启动时会在classpath指定
                               |- *.jar
```

conf.sh该文件是基础环境配置文件。

如果在start.sh后面加入--no-exec-conf-sh参数，则不会从这个文件中读取配置，也包括env.properties，这就需要调用者自己保证环境变量中存在一切需要的配置。（这种情况，特别适合配合PaaS平台，例如k8s+docker来做环境变量注入）

```
## JAVA运行命令
JAVA=

## JVM运行参数
JVM_OPTIONS="-Xms128m -Xmx128m"

## Spring profile名称，用于一套代码多套部署
SPRING_PROFILE="mysql.rowbase51-nontrx"

## 如果需要定制化消费者，则可以将可插拔的jar放入classpath，这里面设置这个jar的Spring XML配置入口即可，见第3大部分详细解读。
## 留空代表不设置，使用默认的consume actor，只打印增量到日志和console
CONSUME_ACTOR_XML_CLASSPATH="fountain-consumer-demo.xml"

## fountain运行时使用的环境变量，优先级最高，会覆盖掉*.properties中的配置。多个文件用空格分隔。
FOUNTAIN_PROPERTIES=("env.properties")
```

### 步骤三：运行
```
start.sh
```

console打印：
```
Starting fountain ...
Fountain started.
```
表示成功。

日志可以进入/log文件夹查看。

### 步骤四：停止
```
stop.sh
```
console打印：
```
Stopping fountain ...
Fountain process 4608 will be shutdown shortly.
Fountain stopped.
```


## 2. 配置说明
### 2.1 profile配置

其中一套代码多套部署的SPRING_PROFILE可用配置如下：
* mysql.rowbase51	Oracle官方5.1
* mysql.rowbase51-nontrx	Oracle官方5.1，无事务支持
* mysql.rowbase56	Oracle官方5.6
* mysql.rowbase56-nontrx	Oracle官方5.6，无事务支持

### 2.2 运行时参数指定

FOUNTAIN_PROPERTIES文件中的值主要是覆盖Spring XML中placeholder配置的properties的。

下面所有的可覆盖的kv，注意key一定不能修改，可以选择覆盖，例如IP:PORT，用户名密码等是一定要指定好的。

```
############################
#        MySQL配置
############################
# mysql用户名密码
mysql_username=beidou
mysql_password=u7i8o9p0

# 当同步点存储文件不存在时候，使用该配置来进行binlog dump，如果置空，则从MySQL Server最后的点开始
# 可以通过类似命令寻找同步点，"show binlog events in 'mysql-bin.000007' from positio limit 10"
mysql_binlogdump_filename=mysql-bin.000120
mysql_binlogdump_position=4

# 百度mysql 5.1版本支持的gtid，小于0则表示不使用配置中的
#mysql_binlogdump_gtid=-1

# MySQL 5.6中，当同步点存储文件不存在时候，使用该配置来进行binlog dump，如果置空，则从MySQL Server最后的点开始
#mysql_binlogdump_gtidset=3f149314-9cad-11e5-8b30-00259089db03:1-252

# 主mysql地址，端口，slaveId（与其他slave不能重复，由于主备不会同一时间连，则配置中可以相同）
#mysql_shard_0_server=172.20.133.119
mysql_shard_0_server=192.168.1.103
mysql_shard_0_port=3308
mysql_shard_0_slaveId=10

# 备mysql
#mysql_shard_0_ha1_server=172.20.133.119
mysql_shard_0_ha1_server=192.168.1.103
mysql_shard_0_ha1_port=3308
mysql_shard_0_ha1_slaveId=10

# 多数据源配置
#dbArray=10.94.37.23:8759,10.94.37.23:8759,10.94.37.23:8759
#bakDbArray=10.94.37.23:8759,10.94.37.23:8759,10.94.37.23:8759
#syncPointArray=-1,-1,-1
#slaveIdArray=10,11,12


############################
#    fountain运行程序配置
############################
# 同步点存储的文件夹地址
#producer_position=/Users/baidu/work/fountain/mysql.rowbase56

# fountain-producer和consumer直接的缓冲队列长度
#memq_limit=60000

# 使用MiniTransactionPolicy策略，一个事务中最大处理的行数
#trans_max_size=50000

### 按照表名进行过滤时，表名格式为database.table（可以为正则），以逗号分隔
### 当白名单和黑名单同时存在时,只有不在黑名单中同时在白名单中存在的才起作用
filter_shard_table_white=
filter_shard_table_black=

############################
#     同步线程高级配置
############################
# 下面是mysql的一些高级设置，一般情况下不建议修改
# replication或者query socket的一些初始化参数
#mysql_wait_timeout=999999
#mysql_net_write_timeout=240
#mysql_net_read_timeout=240
#mysql_charset=binary

# 当主mysql长时间接收不到任何event时，切换到另外一个备mysql的超时时间，单位为毫秒，不配置默认60s
#mysql_replication_socket_so_timeout=120000

# mysql复制线程的一些基础socket参数，timeout单位为毫秒
#mysql_replication_socket_in_buf=16384
#mysql_replication_socket_out_buf=16384
#mysql_replication_socket_connect_timeout=3000
```

## 3. 可插拔消费者
可实现自定义地发布输出增量到下游，原理如下：

net.neoremind.fountain.consumer.spi.Consumer引用ConsumeActor，支持可插拔设计。

通过运行时动态的注解PluggableConsumeActorEnabled扫描Consumer， 去指定的classpath下load一个Spring的XML配置，新建一个IoC容器，作为主容器的孩子。然后将这个新容器中的ConsumeActor覆盖到主容器中Consumer的属性引用，就可以做到一个可插拔的特性。

默认使用打印到console上的简单的ConsumeActor。

![](http://neoremind.com/wp-content/uploads/2016/09/fountain-consume-actor1.png)

### 步骤一：实现个性化的消费Actor

一个简单工程如下：

![](http://neoremind.com/wp-content/uploads/2016/09/project.png)

StdoutConsumeActor：

```
/**
 * 打印到控制的简单消费Actor
 *
 * @author zhangxu
 */
public class StdoutConsumeActor extends DefaultConsumeActor implements ConsumeActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdoutConsumeActor.class);

    @Override
    public void onReceive(ChangeDataSet event) {
        LOGGER.info("=============================");
        LOGGER.info("=       Got new event       =");
        LOGGER.info("=============================");
        printTableDef(event);
        printTableData(event);
    }

    @Override
    public void onSuccess(ChangeDataSet event, DisposeEventPositionBridge bridge) {
        if (bridge != null) {
            bridge.getDisposeEventPosition(event.getInstanceName()).saveSyncPoint(event.getSyncPoint());
        }
        LOGGER.info("Process done!");
    }

    @Override
    public void onUncaughtException(ChangeDataSet event, Exception e) {
        LOGGER.error("Process failed due to {}", e.getMessage(), e);
    }

    /**
     * 打印表结构
     *
     * @param changeDataSet 数据变化
     */
    protected void printTableDef(ChangeDataSet changeDataSet) {
        Map<String, List<ColumnMeta>> tableDef = changeDataSet.getTableDef();
        LOGGER.info("TableDef: {}", tableDef);
    }

    /**
     * 打印变化的数据
     *
     * @param changeDataSet 数据变化
     */
    protected void printTableData(ChangeDataSet changeDataSet) {
        Map<String, List<RowData>> tableData = changeDataSet.getTableData();
        for (String tableName : tableData.keySet()) {
            LOGGER.info("TableName: " + tableName);
            for (RowData rowData : tableData.get(tableName)) {
                LOGGER.info("Before:" + rowData.getBeforeColumnList());
                LOGGER.info("After:" + rowData.getAfterColumnList());
            }
        }
    }

}
```

fountain-consumer-stdout.xml如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

    <description>Consumer配置</description>

    <bean id="stdOutConsumeActor"
          class="net.neoremind.fountain.mq.consumer.StdoutConsumeActor">
    </bean>

</beans>
```

mvn package命名打包成mq-consumer.jar。


### 步骤二：配置
将第一步的jar放到/lib目录。

设置conf.sh中的CONSUME_ACTOR_XML_CLASSPATH=fountain-consumer-stdout.xml。

###步骤三：运行
直接运行即可，控制台会打印：
```
[INFO]  2016-03-18 12:59:31,080 [main]  fountain.runner.CustomConsumerFactoryPostProcessor      (CustomConsumerFactoryPostProcessor.java:91)    -Start to init IoC container by loading XML bean definitions from classpath:fountain-consumer-stdout.xml
```


