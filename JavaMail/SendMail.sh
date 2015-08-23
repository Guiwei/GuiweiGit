#!/bin/bash

SERVER_HOME=/home/gssalert/RedHatMail/build
CLASSPATH=.:$CLASSPATH:/home/gssalert/RedHatMail/build/classes
SERVER_LIB_PATH=/home/gssalert/RedHatMail/WebContent/WEB-INF/lib

#Base library
for  lname in `ls -rt $SERVER_LIB_PATH/*.jar | awk -F\/ '{print \$NF}'`
do
        CLASSPATH=$CLASSPATH:$SERVER_LIB_PATH/$lname
done

export CLASSPATH
echo "____________CLASSPATH_______________"
echo $CLASSPATH
echo "____________CLASSPATH_______________"

#run
#cd $SERVER_HOME

for ANA_STA_PID in `ps -e -o pid -o args | grep SendMail | grep java |awk '{print $1}'`
do
        echo "SendMail is running,please wait is done"
        exit 0
done

echo "Start SendMail ... ...."
file=`date +'%Y%m%d'`

nohup $JAVA_HOME/bin/java com.forlink.http.SendMail  1>>$SERVER_HOME/logs/SendMail.$file.log 2>&1 &

sleep 10
for ANA_STA_PID in `ps -e -o pid -o args | grep SendMail | grep java |awk '{print $1}'`
do
        echo "SendMail PID: "$ANA_STA_PID
done
