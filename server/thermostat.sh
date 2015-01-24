#!/bin/bash

SERVICE_NAME=thermostat
LOG_DIR=/var/log/${SERVICE_NAME}
CONF_DIR=/etc/${SERVICE_NAME}
JAR_PATH=/home/pi/smartphone-thermostat/server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
PID_PATH_NAME=/var/run/${SERVICE_NAME}.pid

case $1 in
    start)
        if [ ! -f $PID_PATH_NAME ]; then
            echo "Starting $SERVICE_NAME..."
            # archive possible previous log files
            cd $LOG_DIR
            NOW=`date +%s`
            mv ${SERVICE_NAME}.log ${SERVICE_NAME}_${NOW}.log 2> /dev/null
            mv status.txt status_${NOW}.txt 2> /dev/null
            # start Java process
            nohup java -Dpath.log="${LOG_DIR}" -Dpath.conf="${CONF_DIR}" -jar $JAR_PATH >> ${SERVICE_NAME}.log 2>&1 &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started"
        else
            echo "$SERVICE_NAME is already running"
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=`cat $PID_PATH_NAME`
            echo "$SERVICE_NAME stoping..."
            kill $PID
            echo "$SERVICE_NAME stopped"
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running"
        fi
    ;;
    *)
        echo "Usage: /etc/init.d/$SERVICE_NAME {start|stop}"
        exit 1
    ;;
esac 