#!/bin/bash
rm -rf nohup.out
export LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH
ps -ef | grep dm_demo | grep -v grep | awk '{print $2}' | xargs kill -9
nohup java -jar dm_demo-1.0.jar &
