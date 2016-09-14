要运行测试工程，可以使用如下步骤
1、build
clean package -Pdev -Dmaven.test.skip=true
这个会打包生成tar.gz，内含
lib/*.jar（其中fountain-example.jar不包含任何properties和xml）
run.sh
resources/*.xml和*.properties

2、配置properties和xml
包括数据源和用户名密码
log4j日志级别
使用哪个消费者，默认是BeidouAddbConsumer
eventpostion持久化文件存储路径

3、修改run.sh
中的如下变量：
JAVA_HOME_BIN=/home/work/local/jdk1.6.0_30/bin/
SPRING_XML="classpath:fountain-config.xml"

所有输出打印到output.log里面

4、启动
nohup sh run.sh & 后台启动

sh run.sh前台可以启动，输入stop停止运行。

