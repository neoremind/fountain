#!/bin/sh

# Note: This script MUST be launched under /bin directory

CURR_PATH=`pwd`
ROOT_PATH=`cd $CURR_PATH/.. && pwd`
PIDFILE="${ROOT_PATH}"/fountain-runner.pid

echo "Stopping fountain ... "
if [ ! -f "$PIDFILE" ];then
    echo "No fountain to stop (could not find file $PIDFILE)."
else
    echo "Fountain process "`cat $PIDFILE`" will be shutdown shortly."
    kill -9 `cat "$PIDFILE"`
    rm "$PIDFILE"
    echo "Fountain stopped."
fi
exit 0