测试的方法主要是异步启动producer和consumer线程，然后主动去执行sql，监控到增量变化做assert。

测试src目录如下：
dao目录下是带回滚的关于student和city表的sql测试，不涉及fountain
it/template是测试的模板类
it/baidu/rowbased51是测试的百度MySQL Ares 5.1版本
it/baidu/rowbased56，由于和官方5.6没区别，因此这里不覆盖了
it/oracle/rowbased51是测试的官方MySQL5.1版本
it/oracle/rowbased56是测试的官方MySQL5.6版本

测试resouces目录同上。

=============
官方MySQL 5.1测试在本机：
mysql -h172.20.133.64 -ubeidou -pu7i8o9p0 -P3308

官方MySQL 5.6测试在本机：
mysql -h10.94.37.23 -ubeidou -pu7i8o9p0 -P8769

百度MySQL Ares 5.1
mysql -h10.94.37.23 -ubeidou -pu7i8o9p0 -P8759