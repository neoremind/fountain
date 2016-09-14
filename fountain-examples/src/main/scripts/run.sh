#!/bin/sh

# leave blank if java include in PATH
JAVA_HOME_BIN=/home/work/local/jdk1.6.0_30/bin/

SPRING_XML="classpath:fountain-config.xml"

ROOT_PATH=`pwd`
LOG_FILE="${ROOT_PATH}"/output.log

CUR_CLASSPATH='.'
for f in `ls "${ROOT_PATH}"/lib/*.jar`
do
    CUR_CLASSPATH=${CUR_CLASSPATH}:$f
done

CUR_CLASSPATH="${ROOT_PATH}/resources:"${CUR_CLASSPATH}

${JAVA_HOME_BIN}java -Xms512m -Xmx512m -classpath ${CUR_CLASSPATH} net.neoremind.fountain.test.cmd.FountainApplicationLauncher ${SPRING_XML} >> ${LOG_FILE}

msg="执行发生错误！"
if [ $? -ne 0 ]
then
    echo "${msg}"
fi