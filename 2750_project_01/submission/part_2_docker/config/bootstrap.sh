#!/bin/bash

service ssh start

/usr/local/hadoop/sbin/start-dfs.sh
/usr/local/hadoop/sbin/start-yarn.sh

# /usr/local/hadoop/sbin/mr-jobhistory-daemon.sh --config /usr/local/hadoop/etc/hadoop start historyserver
/usr/local/hadoop/bin/mapred --daemon start historyserver


/bin/bash
