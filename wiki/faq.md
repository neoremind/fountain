# FAQ

### 1、底层通信模型？如何解决网络抖动导致丢包问题？
采用BIO作为底层网络模型，BIO的优势是支持连接和读取超时，而且当前场景内Producer线程和其他线程及UI没有交互，比较适合BIO模型。
当网络抖动导致丢包时，底层socket不能准确的获取通信状态，使用BIO的读取或写入超时来解决，当超过限定的时间（缺省为1分钟）不能读取或写完数据Fountain就认为网络或者数据源出现问题，此时需要重新建立连接。

###2、数据源重连策略？
当Fountain认为数据源或者网络出现问题时，它首先会尝试重新连接一次当前数据源，如果连接失败，Fountain认为当前数据源实例出现故障，此时它会自动连接备用数据源，如果连接备用数据源也失败，Fountain会sleep一段时间后持续连接下去。Sleep默认5s，可以通过producer的repeatResourceInterval属性进行设置。

### 3、如何获取表的元数据？
对于row base binlog方式，使用"show full fields from ${table name}" sql来获取表结构，通过string描述解析出表元信息，包括字段的charset信息。表的元数据信息会被缓存，判断表结构是否发生变化的依据是对于同一表名的table id是否发生变化。Table id是mysql内部对表元数据缓存的编号，table id会被写入binlog日志。如果表结构发生变化，table id一定发生变化，但反之不成立。下一个版本将使用mysql的show field list指令代替"show full fields from ${table name}" sql，也不再使用字符串匹配来解析元信息。

### 4、如何控制事务？
从binlog中解析出的数据原则上都是已提交事务的数据，但有一种情况例外，当在一个事务中同时操作innodb和myisam（或者其他不支持事务的引擎）时，如果操作myisam后事务失败，mysql依然会把数据写入到binlog日志中，但在事务的最后记录"rollback"日志。

对于纯innodb的数据库或者databus，建议Fountain不控制事务，这样能保证高效并且内存消耗少。Databus内部自然忽略被rollback的事务。

对于必须使用事务控制的场景，Fountain实现了MiniTransactionPolicy，首先它需要一个数据条数上限，Fountain不能准确的控制内存的大小，它只能粗粒度的控制数据的条数，当事务数据超过最大条数，Fountain将丢弃该事务的数据并记录日志。MiniTransactionPolicy会积累不超过上限的数据然后整事务控制处理。

### 5、同步点记录？
对于GT Id同步点的记录，采用延迟记录方式实现，因此mysql是从GT Id + 1个事务开始接收增量，延迟一个事务记点保证Fountain实例恢复时不丢失数据。Fountain会重复发送数据，因此下游需要控制去重。

对于非GT Id同步点的记录，使用定时器扫描所有mysql实例的日志点并记录。

同步点和同步点的记录工具被抽象成接口，因此可以有多种实现，Fountain默认实现文件和zookeeper记录同步点。对于zookeeper模式，为防止频繁网络操作，Fountain实现延时记录功能。

### 6、Load data into处理？
Fountain不推荐使用load data。对于load data加载数据的表请使用基准下发。

### 7、Fountain实例故障转移实现？
Fountain使用zookeeper协调冷、热Fountain实例。

### 8、Text和blob的类型区分？
在mysql底层Text和blob存储实现相同，都是存储byte数据。在Fountain中如果使用rowbase binlog方式，是根据字段类型的名称来区分的，如果字段以text or char结尾，就认为该字段是String。

### 9、支持超时打包下发bigpipe实现？
Bagpipe包的最大容量是2M，当数据积累超过2M时需要下发，同时为防止空闲时数据迟迟不能下发需要超时功能。使用SynchronousQueue和一个线程配合解决问题。Fountain下发线程把数据推送到SynchronousQueue，另一线程从SynchronousQueue读取数据放入包中并记录上次发送的时间点，如果包已满或者当前司机和上次发送的时间点之差大于或者等于超时时间则发送数据包。两个线程之间数据的流向是单向的，线程之间没有直接的状态依赖，所以每一个线程都不需要使用线程同步，这样提高了性能也简化了编程的逻辑。SynchronousQueue相较于其他线程安全的Queue的优点是SynchronousQueue直接连接两个线程，不需要额外的内存缓冲区。

### 10、账户不具备同步权限会发生什么错误？
```
Receive Error Packet! Error code is 34952, sqlstateMarker is 35, sqlstate is [56, 56, 83, 56, 56], error message is denied command -_-||

或者

java.io.IOException: Error When doing Client Authentication:1045, Access denied for user 'fountainsync'@'10.46.152.32' (using password: YES)
```