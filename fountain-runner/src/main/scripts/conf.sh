#!/bin/sh

## 注意：如果环境变量提供了以下的所有配置，则可以在命令上加入--no-exec-conf-sh来禁止下面的变量覆盖

## JAVA运行命令
JAVA=

## JVM运行参数
JVM_OPTIONS="-Xms256m -Xmx256m"

## Spring profile名称，用于一套代码多套部署
SPRING_PROFILE="mysql.rowbase51-nontrx"

## 如果需要定制化消费者，则可以将可插拔的jar放入classpath，这里面设置这个jar的Spring XML配置入口即可
## 留空代表不设置，使用默认的consume actor，只打印增量到日志和console
CONSUME_ACTOR_XML_CLASSPATH="fountain-consumer-demo.xml"

## fountain运行时使用的环境变量，优先级最高，会覆盖掉*.properties中的配置。多个文件用空格分隔。
FOUNTAIN_PROPERTIES=("env.properties")

