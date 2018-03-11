export MASTER_IP=159.203.71.174
export SLAVE_IP=159.203.167.80

cd /usr/local/hadoop

#start cluster
sbin/start-dfs.sh
sbin/start-yarn.sh

#start job history server
$HADOOP_PREFIX/sbin/mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR start historyserver

#test with jps
jps
ssh slave
jps
exit

# test with create an directory on HDFS
export PATH=$PATH:$HADOOP_PREFIX/bin
hdfs dfs -mkdir /input
hdfs dfs -ls /
hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/root
hdfs dfs -put etc/hadoop/ input
hdfs dfs -ls /user/root/input

bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.0.0.jar pi 2 5

bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.0.0.jar wordcount input/ output/
hdfs dfs -cat output/*
hdfs dfs -rmr output