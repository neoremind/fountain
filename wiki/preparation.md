# 准备工作

## 1. Java版本依赖
JDK1.6以及1.6+

## 2. 依赖
下述各模块最新可用版本请[参考Release Notes](release_notes.md)。

Maven:
```
<dependency>
    <groupId>net.neoremind</groupId>
    <artifactId>fountain-producer</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>net.neoremind</groupId>
    <artifactId>consumer-spi</artifactId>
    <version>1.0.0</version>
</dependency>
```

注意：fountain整体依赖Spring，默认版本是3.1.2.RELEASE，如工程使用其他版本Spring，可自行exclude掉。

## 3. 日志
使用slf4j作为日志接口，默认依赖log4j作为日志输出实现，如使用方使用logback，则可以exclude掉log4j，配置POM如下：
```
<exclusions>
    <exclusion>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-log4j12</artifactId>
    </exclusion>
<exclusions>
```
请在log4j.properties中配置日志级别，注意打开DEBUG可能会影响部分性能。
```
log4j.logger.com.baidu.fountain=INFO
```

## 4. MySQL条件
请首先确认MySQL开启了binlog，并且使用ROW模式。
```
mysql> show variables like 'binlog_format';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
1 row in set (0.01 sec)

mysql> show variables like 'log_bin';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | ON    |
+---------------+-------+
1 row in set (0.01 sec)
```

查看mysql binlog文件的命令如下：
```
mysql> show binary logs;
+------------------+-----------+
| Log_name         | File_size |
+------------------+-----------+
| mysql-bin.000001 |         0 |
| mysql-bin.000002 |         0 |
| mysql-bin.000003 |         0 |
| mysql-bin.000004 |   9547101 |
+------------------+-----------+
4 rows in set (0.01 sec)
```

如果使用mysql binlog dump，请确保用户拥有复制权限。

## 5. 使用API方式运行

见Quick Start部分。

## 6. 使用Spring XML配置运行
fountain的生命周期完成交由调用代码，会在内部异步启动两个线程，一个用于接收binlog日志推送，一个用于消费变化，一旦调用代码进程退出，则终止该过程。

默认fountain的配置文件放在fountain-config.xml中，测试方法上可以使用System.read()阻塞住，

可以参考[Fountain-examples](https://github.com/neoremind/fountain/tree/master/fountain-examples/src/test/java/net/neoremind/fountain/examples/inprocess)
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:fountain-config.xml"})
public class BaiduAresRowbase51 {
    @Test
    public void test() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

在生产环境可以配合tomcat、jetty等容器托管，只要容器存活，fountain即不断接口消费变化，请在web.xml中加入如下配置，或者将fountain-config.xml加入spring主配置applicationContext.xml入口中：
```
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:fountain-config.xml</param-value>
</context-param>
<listener>
    <listener-class>
        org.springframework.web.context.ContextLoaderListener
    </listener-class>
</listener>
```