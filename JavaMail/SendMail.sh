#!/bin/bash

PROJECT_PATH=/media/guiwei/Files/GuiweiGit/JavaMail
CLASSPATH=.:$CLASSPATH:$PROJECT_PATH/build/classes
SERVER_LIB_PATH=$PROJECT_PATH/WebContent/WEB-INF/lib

#Base library
for lname in `ls -rt $SERVER_LIB_PATH/*.jar | awk -F\/ '{print \$NF}'`
do
	CLASSPATH=$CLASSPATH:$SERVER_LIB_PATH/$lname
done

export CLASSPATH

for ANA_STA_PID in `ps -e -o pid -o args | grep SendMail | grep java |awk '{print $1}'`
do
	echo "You've a unfinished SendMail."
	exit 0
done

today=`date +'%Y%m%d'`
nohup $JAVA_HOME/bin/java com.z.mail.SendMail 1 > SendMail.$today.log 2>&1 &

sleep 2
for ANA_STA_PID in `ps -e -o pid -o args | grep SendMail | grep java |awk '{print $1}'`
do
	echo "SendMail PID: "$ANA_STA_PID
done
