#! /bin/sh

echo -n "Stopping Lecturesight: "

LS_PID=`ps aux | grep java | awk '/lecturesight/ && !/awk/ {print $2}'`
if [ -z "$LS_PID" ]; then
  echo "Lecturesight already stopped"
  exit 1
fi

kill $LS_PID

sleep 10

LS_PID=`ps aux | grep java | awk '/lecturesight/ && !/awk/ {print $2}'`
if [ ! -z $LS_PID ]; then
  echo "Hard killing since felix ($LS_PID) seems unresponsive to regular kill"
  kill -9 $LS_PID
fi

echo "done."

