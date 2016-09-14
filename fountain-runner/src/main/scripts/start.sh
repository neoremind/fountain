#!/bin/sh

# Note: This script MUST be launched under /bin directory

CURR_PATH=`pwd`
CURR_DATE=`date +%Y%m%d`
ROOT_PATH=`cd $CURR_PATH/.. && pwd`
LOG_PATH=$ROOT_PATH/log
if [ ! -d "$LOG_PATH" ]; then
    mkdir -p $LOG_PATH
fi
LOG_FILE="${LOG_PATH}"/fountain-runner.log
PIDFILE="${ROOT_PATH}"/fountain-runner.pid

if [[ "$1" =~ "no-exec-conf-sh" ]]; then
    echo "env.sh will NOT be used to define enviroment variables. Make sure you have set them properly."
else
    CONF_SH="conf.sh"
    [ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
fi

if [ -n $JAVA  ] || [ -z $JAVA ]; then
    echo "No JAVA command specified and try to use \$JAVA_HOME or just \"java\" instead."
    if [ "$JAVA_HOME" != "" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA=java
    fi
fi

for prop in ${FOUNTAIN_PROPERTIES[@]};
do
    source $prop
done

CUR_CLASSPATH='.'
for f in `ls "${ROOT_PATH}"/lib/*.jar`
do
    CUR_CLASSPATH=${CUR_CLASSPATH}:$f
done

CUR_CLASSPATH="${ROOT_PATH}/resources:"${CUR_CLASSPATH}

echo "Starting fountain ... "
if [ -f PIDFILE ]; then
    if kill -0 `cat PIDFILE` > /dev/null 2>&1; then
        echo $command already running as process `cat PIDFILE`.
        exit 0
    fi
fi

# 为了可以将环境变量带到子shell中
source export.sh
nohup $JAVA $JVM_OPTIONS -classpath ${CUR_CLASSPATH} -Dspring.profiles.active=$SPRING_PROFILE -Dconsume.actor.xml.classpath=$CONSUME_ACTOR_XML_CLASSPATH net.neoremind.fountain.runner.FountainMain >> ${LOG_FILE} 2>&1 < /dev/null &

if [ $? -eq 0 ]
then
    if /bin/echo -n $! > "$PIDFILE"
    then
        sleep 1
        echo "Fountain started."
        exit 0
    else
        echo "Failed to write pid file."
        exit 1
    fi
else
    echo "Fountain failed to launch."
    exit 1
fi
